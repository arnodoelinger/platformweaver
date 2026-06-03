package io.github.arsmotorin.ofrat.gradle

import io.github.arsmotorin.ofrat.compiler.PlatformCommandLineProcessor.Companion.PLUGIN_ID
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

/**
 * Gradle plugin that wires `OFRAT` into any Kotlin project.
 *
 * Registers the compiler plugin and forwards the configured [OfratExtension.target] platform
 * as a `-P plugin:...:platform=<target>` compiler argument.
 *
 * ## Usage
 *
 * ```kotlin
 * // build.gradle.kts
 * plugins {
 *     id("io.github.arsmotorin.ofrat")
 * }
 *
 * ofrat {
 *     target = "paper"    // Or "fabric", "neoforge", or any custom string
 * }
 *
 * dependencies {
 *     compileOnly("io.github.arsmotorin:ofrat-annotations:VERSION")
 *     "kotlinCompilerPluginClasspath"("io.github.arsmotorin:ofrat:VERSION")
 * }
 * ```
 */
class OfratGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("ofrat", OfratExtension::class.java)

        project.afterEvaluate {
            val target = extension.target?.trim()?.lowercase()
            if (target.isNullOrBlank()) return@afterEvaluate

            project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
                task.compilerOptions.freeCompilerArgs.addAll(
                    "-P", "plugin:$PLUGIN_ID:platform=$target"
                )
            }

            registerChameleonCodegen(project, extension, target)
        }
    }

    /**
     * Wires the `@Chameleon` codegen task: parses carriers under [OfratExtension.chameleonsDir],
     * generates per-platform `typealias` files into a build directory, adds that directory to the
     * Kotlin compilation source, and makes compilation depend on the generation.
     */
    private fun registerChameleonCodegen(project: Project, extension: OfratExtension, target: String) {
        val dirPath = extension.chameleonsDir?.trim() ?: return
        val outputDir = File(project.layout.buildDirectory.get().asFile, "generated/ofrat/chameleons")

        val task = project.tasks.register("generateChameleons", GenerateChameleonsTask::class.java) { t ->
            t.platform.set(target)
            t.outputDir.set(outputDir)
            val carriersDir = project.file(dirPath)
            if (carriersDir.exists()) t.chameleonsDir.set(project.layout.projectDirectory.dir(dirPath))
        }

        project.tasks.withType(KotlinCompile::class.java).configureEach { compile ->
            compile.dependsOn(task)
            compile.source(outputDir)
        }
    }
}
