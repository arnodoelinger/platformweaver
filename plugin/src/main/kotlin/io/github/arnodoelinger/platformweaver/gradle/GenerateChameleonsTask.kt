package io.github.arnodoelinger.platformweaver.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Gradle task that realizes `@Chameleon` carriers into per-platform declarations — type aliases,
 * extension / computed properties, and functions.
 *
 * Reads every `.kt` file under [chameleonsDir], delegates parsing and generation to
 * [ChameleonGenerator] for the configured [platform], and writes the result into [outputDir]
 * (which the `Platform Weaver` Gradle plugin wires onto the Kotlin compilation's source set).
 */
abstract class GenerateChameleonsTask : DefaultTask() {
    /** Directory holding the `@Chameleon` carrier files. Not itself a compiled source root. */
    @get:InputDirectory @get:Optional abstract val chameleonsDir: DirectoryProperty

    /** Target platform key (e.g. `"paper"`); carriers for other platforms are skipped. */
    @get:Input abstract val platform: Property<String>

    /** Directory the generated `typealias` files are written to. */
    @get:OutputDirectory abstract val outputDir: DirectoryProperty

    /** Parses the carriers and writes the generated aliases for the active platform. */
    @TaskAction fun generate() {
        val out = outputDir.get().asFile
        out.deleteRecursively()
        out.mkdirs()

        val dir = chameleonsDir.orNull?.asFile
        if (dir == null || !dir.exists()) return

        val sources = dir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .map { ChameleonGenerator.SourceFile(it.readText()) }
            .toList()

        ChameleonGenerator.generate(sources, platform.get()).forEach { generated ->
            File(out, generated.relativePath).apply {
                parentFile.mkdirs()
                writeText(generated.content)
            }
        }
    }
}
