package io.github.arsmotorin.ofrat.gradle

/**
 * Codegen behind `@Chameleon`.
 *
 * Parses lightweight `val` carriers annotated with a platform annotation plus `@Chameleon`, and
 * emits a real `typealias` for the requested platform only. Keeping this logic free of any Gradle
 * type makes it directly unit-testable; [GenerateChameleonsTask] is a thin wrapper around it.
 *
 * The parser is intentionally simple, so it matches the constrained carrier grammar by regex rather
 * than running a full Kotlin frontend:
 *
 * ```kotlin
 * @PaperOnly @Chameleon val PlatPlayer: org.bukkit.entity.Player
 * @FabricOnly @Chameleon val PlatPlayer: net.minecraft.server.level.ServerPlayer
 * ```
 */
object ChameleonGenerator {
    /** A source file fed to the generator. [content] is the raw Kotlin text. */
    data class SourceFile(val content: String)

    /** A generated file: [relativePath] under the output dir, with Kotlin [content]. */
    data class GeneratedFile(val relativePath: String, val content: String)

    /** A single parsed `@Chameleon` carrier. */
    private data class Carrier(val platform: String, val name: String, val type: String, val pkg: String)

    /** Built-in platform annotation simple names mapped to their platform key. */
    private val BUILTIN_PLATFORMS = mapOf(
        "FabricOnly" to "fabric",
        "PaperOnly" to "paper",
        "NeoForgeOnly" to "neoforge",
    )

    private val PACKAGE = Regex("""(?m)^\s*package\s+([\w.]+)""")

    /** A run of annotations immediately followed by `val Name: Type` (type captured to line end). */
    private val CARRIER = Regex("""((?:@[\w.]+(?:\([^)]*\))?\s+)+)val\s+(\w+)\s*:\s*([^\n/=]+)""")

    /** Matches each `@Name` (optionally `@Name("arg")`) inside an annotation run. */
    private val ANNOTATION = Regex("""@([\w.]+)(?:\(\s*"([^"]*)"\s*\))?""")

    /**
     * Generates the platform `typealias` declarations for [platform] from the chameleon [sources].
     *
     * Carriers targeting other platforms are skipped. The result is grouped into one file per
     * package so each generated `typealias` lands in the package its carrier declared.
     */
    fun generate(sources: List<SourceFile>, platform: String): List<GeneratedFile> {
        val target = platform.trim().lowercase()
        val carriers = sources.flatMap { parse(it.content) }.filter { it.platform == target }

        return carriers.groupBy { it.pkg }.map { (pkg, group) ->
            val header = if (pkg.isEmpty()) "" else "package $pkg\n\n"
            val aliases = group
                .distinctBy { it.name }
                .joinToString("\n") { "typealias ${it.name} = ${it.type.trim()}" }
            val fileName = if (pkg.isEmpty()) "Chameleons.kt" else "${pkg.replace('.', '/')}/Chameleons.kt"
            GeneratedFile(fileName, "$header$aliases\n")
        }
    }

    /** Extracts every chameleon carrier from a single file's [content]. */
    private fun parse(content: String): List<Carrier> {
        val pkg = PACKAGE.find(content)?.groupValues?.get(1).orEmpty()
        return CARRIER.findAll(content).mapNotNull { match ->
            val annotationRun = match.groupValues[1]
            val name = match.groupValues[2]
            val type = match.groupValues[3]

            val annotations = ANNOTATION.findAll(annotationRun).toList()
            val isChameleon = annotations.any { it.groupValues[1].substringAfterLast('.') == "Chameleon" }
            if (!isChameleon) return@mapNotNull null

            val platform = annotations.firstNotNullOfOrNull { platformOf(it) } ?: return@mapNotNull null
            Carrier(platform, name, type, pkg)
        }.toList()
    }

    /** Resolves the platform key carried by a single annotation match, or null if it carries none. */
    private fun platformOf(match: MatchResult): String? {
        val simpleName = match.groupValues[1].substringAfterLast('.')
        val explicitValue = match.groupValues[2]
        return when {
            simpleName == "PlatformOnly" && explicitValue.isNotEmpty() -> explicitValue.lowercase()
            else -> BUILTIN_PLATFORMS[simpleName]
        }
    }
}
