package io.github.arnodoelinger.platformweaver

/**
 * Marks a declaration as `NeoForge`-specific.
 *
 * When compiling for any platform other than `"neoforge"`, `Platform Weaver` removes every declaration
 * annotated with `@NeoForgeOnly` from the compiler output.
 *
 * ## Usage
 *
 * ```kotlin
 * object EventBus {
 *     // Compiled only into the NeoForge JAR:
 *     @NeoForgeOnly fun register(handler: Any) {
 *         NeoForge.EVENT_BUS.register(handler)
 *     }
 *
 *     // Compiled only into the Fabric JAR:
 *     @FabricOnly fun register(handler: Any) {
 *         // Fabric event registration
 *     }
 * }
 * ```
 *
 * ## Classpath note
 *
 * Because the Kotlin compiler resolves all types before the plugin strips declarations,
 * any `NeoForge`-specific types referenced inside a `@NeoForgeOnly` block must still be
 * resolvable at compile time. Add `NeoForge` as `compileOnly` in shared modules.
 *
 * @see PlatformOnly
 * @see FabricOnly
 * @see PaperOnly
 */
@PlatformOnly("neoforge")
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@Retention(AnnotationRetention.SOURCE)
annotation class NeoForgeOnly
