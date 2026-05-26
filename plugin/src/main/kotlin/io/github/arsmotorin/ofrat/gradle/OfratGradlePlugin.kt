package io.github.arsmotorin.ofrat.gradle

import io.github.arsmotorin.ofrat.compiler.PlatformCommandLineProcessor.Companion.PLUGIN_ID
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
        }
    }
}
