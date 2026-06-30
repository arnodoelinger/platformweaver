@file:OptIn(org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI::class)

package io.github.arnodoelinger.ofrat.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 * `OFRAT`'s core IR transformer. Strips platform-specific declarations at compile time.
 *
 * ## Algorithm
 *
 * For each declaration in the module:
 * 1. Inspect its annotations.
 * 2. For each annotation, check if the annotation class itself carries
 *    `@PlatformOnly(platform = X)`.
 * 3. If `X != targetPlatform`, remove the declaration from the IR.
 *
 * This runs after frontend type-checking, so both platform APIs must be `compileOnly` on
 * the shared module's classpath.
 *
 * The stripped declarations are absent from bytecode.
 *
 * @param targetPlatform lowercase platform name, e.g. `"paper"`.
 */
internal class PlatformIrTransformer(private val targetPlatform: String) : IrGenerationExtension {
    /** Generate the contexts. */
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transformChildrenVoid(Stripper())
    }

    private inner class Stripper : IrElementTransformerVoid() {
        /** Visit the declaration, and remove the declaration from the IR. */
        override fun visitFile(declaration: IrFile): IrFile {
            declaration.declarations.removeAll { it.isForAnotherPlatform() }
            return super.visitFile(declaration)
        }

        /** Visit the class declaration, and remove the platform-specific members from the IR. */
        override fun visitClass(declaration: IrClass): IrStatement {
            val droppedProperties: Set<IrProperty> = declaration.declarations
                .filterIsInstance<IrProperty>()
                .filter { it.isForAnotherPlatform() }
                .toHashSet()

            declaration.declarations.removeAll { member ->
                when (member) {
                    is IrProperty -> member in droppedProperties
                    is IrSimpleFunction -> member.isForAnotherPlatform() || member.correspondingPropertySymbol?.owner in droppedProperties
                    is IrClass -> member.isForAnotherPlatform()
                    is IrConstructor -> member.isForAnotherPlatform()
                    else -> false
                }
            }
            return super.visitClass(declaration)
        }
    }

    /** Check if the annotation class itself carries `@PlatformOnly(platform = X)`. */
    private fun IrDeclaration.isForAnotherPlatform(): Boolean =
        annotations.any { it.isForAnotherPlatform() }

    /**
     * Check if the constructor call is annotated with a platform-specific annotation.
     * If the annotation is not present, it is assumed that the platform-specific annotation is
     * not present on the constructor.
     */
    private fun IrConstructorCall.isForAnotherPlatform(): Boolean {
        val annotationClass = runCatching { symbol.owner.parentAsClass }.getOrNull() ?: return false

        val platformOnly = annotationClass.annotations.firstOrNull { meta ->
            runCatching { meta.symbol.owner.parentAsClass }
                .getOrNull()
                ?.fqNameWhenAvailable
                ?.asString() == PLATFORM_ONLY_FQ_NAME
        } ?: return false

        @Suppress("UNCHECKED_CAST")
        val accessExpr = platformOnly as? IrMemberAccessExpression<*> ?: return false
        val platformArg = accessExpr.arguments.getOrNull(0) ?: return false
        val platformValue = (platformArg as? IrConst)?.value as? String ?: return false

        return platformValue.trim().lowercase() != targetPlatform
    }
}
