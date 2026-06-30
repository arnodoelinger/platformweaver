package io.github.arnodoelinger.platformweaver.gradle

import io.github.arnodoelinger.platformweaver.compiler.PlatformCommandLineProcessor.Companion.PLUGIN_ID
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

/**
 * Gradle plugin that wires `Platform Weaver` into any Kotlin project.
 *
 * Implements [KotlinCompilerPluginSupportPlugin] — the same SPI the Kotlin Gradle plugin uses for its
 * own first-party compiler plugins (`kotlinx-serialization`, `kotlin-parcelize`, ...). Applying
 * `id("io.github.arnodoelinger.platformweaver")` is enough on its own: the compiler plugin jar is added
 * to the Kotlin compilation's plugin classpath and the configured [PlatformWeaverExtension.target] is
 * forwarded as a plugin option automatically — no manual `kotlinCompilerPluginClasspath` dependency or
 * `freeCompilerArgs` wiring required.
 *
 * ## Usage
 *
 * ```kotlin
 * // build.gradle.kts
 * plugins {
 *     id("io.github.arnodoelinger.platformweaver")
 * }
 *
 * platformweaver {
 *     target = "paper"    // Or "fabric", "neoforge", or any custom string
 * }
 *
 * dependencies {
 *     // The compiler plugin itself is added automatically. The annotations stay a normal dependency:
 *     compileOnly("io.github.arnodoelinger:platformweaver-annotations:VERSION")
 * }
 * ```
 */
class PlatformWeaverGradlePlugin : KotlinCompilerPluginSupportPlugin {
    /** The [PlatformWeaverExtension] instance. */
    private lateinit var extension: PlatformWeaverExtension

    /** The plugin is applied to the root project. */
    override fun apply(target: Project) {
        extension = target.extensions.create("platformweaver", PlatformWeaverExtension::class.java)
    }

    /** The plugin is applied to all compilations. */
    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    /** The compiler plugin id is used to configure the plugin option. */
    override fun getCompilerPluginId(): String = PLUGIN_ID

    /** The compiler plugin jar is added to the Kotlin compilation's plugin classpath. */
    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "io.github.arnodoelinger",
        artifactId = "platformweaver-plugin",
        version = BuildConfig.VERSION,
    )

    /** Applies the `@Chameleon` codegen task to the compilation. */
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        registerChameleonCodegen(project, kotlinCompilation)

        return project.provider {
            val platformTarget = extension.target?.trim()?.lowercase()
            if (platformTarget.isNullOrBlank()) emptyList()
            else listOf(SubpluginOption("platform", platformTarget))
        }
    }

    /**
     * Wires the `@Chameleon` codegen task for the `main` compilation: parses carriers under
     * [PlatformWeaverExtension.chameleonsDir], generates the per-platform declarations into a build
     * directory, and adds that directory to the compilation's Kotlin source.
     */
    private fun registerChameleonCodegen(project: Project, kotlinCompilation: KotlinCompilation<*>) {
        if (kotlinCompilation.name != "main") return

        val platformTarget = extension.target?.trim()?.lowercase()
        val dirPath = extension.chameleonsDir?.trim()
        if (platformTarget.isNullOrBlank() || dirPath == null) return

        val outputDir = File(project.layout.buildDirectory.get().asFile, "generated/platformweaver/chameleons")
        val task = project.tasks.register("generateChameleons", GenerateChameleonsTask::class.java) { t ->
            t.platform.set(platformTarget)
            t.outputDir.set(outputDir)
            val carriersDir = project.file(dirPath)
            if (carriersDir.exists()) t.chameleonsDir.set(project.layout.projectDirectory.dir(dirPath))
        }

        kotlinCompilation.compileTaskProvider.configure { compileTask ->
            compileTask.dependsOn(task)
            (compileTask as? KotlinCompile)?.source(outputDir)
        }
    }
}
