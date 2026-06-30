package io.github.arnodoelinger.platformweaver.gradle

/**
 * Codegen behind `@Chameleon`.
 *
 * A `@Chameleon` carrier is a platform-annotated declaration that `Platform Weaver` resolves to a
 * single real declaration for the active target, so the shared code that consumes it is written
 * once instead of as a `@PaperOnly` / `@FabricOnly` pair. Three carrier shapes are supported:
 *
 * | Carrier                                          | Emitted for the target platform        |
 * |--------------------------------------------------|----------------------------------------|
 * | `val Name: Type`              (bare type)        | `typealias Name = Type`                |
 * | `val [Recv.]name: T get() = e`  (has a body)     | the property, verbatim                 |
 * | `fun [Recv.]name(p): T = e`                      | the function, verbatim                 |
 *
 * The alias form erases type-name divergence (`Player` vs `ServerPlayer`); the property and
 * function forms absorb accessor and behaviour divergence (`player.uniqueId` vs `player.uuid`),
 * which a type alias cannot express. Bodies may reference imported symbols: every `import` in the
 * carrier files is carried into the generated file when the emitted declarations actually use it.
 *
 * Keeping this logic free of any Gradle type makes it directly unit-testable; [io.github.arnodoelinger.platformweaver.gradle.GenerateChameleonsTask]
 * is a thin wrapper around it. The parser matches the constrained carrier grammar by regex rather
 * than running a full Kotlin frontend, so each carrier is a single line (expression-bodied):
 *
 * ```kotlin
 * @PaperOnly @Chameleon val PlatPlayer: org.bukkit.entity.Player
 * @FabricOnly @Chameleon val PlatPlayer: net.minecraft.server.level.ServerPlayer
 *
 * @PaperOnly @Chameleon val PlatPlayer.platUuid: java.util.UUID get() = uniqueId
 * @FabricOnly @Chameleon val PlatPlayer.platUuid: java.util.UUID get() = uuid
 * ```
 */
object ChameleonGenerator {
    /** A source file fed to the generator. [content] is the raw Kotlin text. */
    data class SourceFile(val content: String)

    /** A generated file: [relativePath] under the output dir, with Kotlin [content]. */
    data class GeneratedFile(val relativePath: String, val content: String)

    /** A single parsed carrier, already resolved into the [declaration] to emit for one platform. */
    private data class Carrier(
        val platform: String,
        val pkg: String,
        val declaration: String,
        val isAlias: Boolean,
    )

    /** Built-in platform annotation simple names mapped to their platform key. */
    private val BUILTIN_PLATFORMS = mapOf(
        "FabricOnly" to "fabric",
        "PaperOnly" to "paper",
        "NeoForgeOnly" to "neoforge",
    )

    private val PACKAGE = Regex("""(?m)^\s*package\s+([\w.]+)""")

    /** `import a.b.C` or `import a.b.C as D`, capturing the path and optional alias. */
    private val IMPORT = Regex("""(?m)^\s*import\s+([\w.]+(?:\.\*)?)(?:\s+as\s+(\w+))?""")

    /** A run of annotations immediately followed by a `val` / `fun` carrier, captured to line end. */
    private val CARRIER = Regex("""(?m)^[ \t]*((?:@[\w.]+(?:\([^)]*\))?[ \t\r\n]+)+)((?:val|fun)\b[^\n]*)""")

    /** Matches each `@Name` (optionally `@Name("arg")`) inside an annotation run. */
    private val ANNOTATION = Regex("""@([\w.]+)(?:\(\s*"([^"]*)"\s*\))?""")

    /** A `val [Receiver.]Name : <rest>` carrier, capturing the bare name and everything after `:`. */
    private val VAL_CARRIER = Regex("""^val\s+(?:[\w.]+\.)?(\w+)\s*:\s*(.+)$""")

    /** True when a `val` carrier declares an extension receiver (`val Receiver.name: ...`). */
    private val VAL_HAS_RECEIVER = Regex("""^val\s+[\w.]+\.\w+\s*:""")

    /**
     * Generates the platform declarations for [platform] from the chameleon [sources].
     *
     * Carriers targeting other platforms are skipped. The result is grouped into one file per
     * package so each generated declaration lands in the package its carrier declared.
     */
    fun generate(sources: List<SourceFile>, platform: String): List<GeneratedFile> {
        val target = platform.trim().lowercase()
        val carriers = sources.flatMap { parse(it.content, target) }
        if (carriers.isEmpty()) return emptyList()

        val imports = collectImports(sources)

        return carriers.groupBy { it.pkg }.map { (pkg, group) ->
            val declarations = group.map { it.declaration }.distinct()
            // Only member declarations (not type aliases, which spell out fully-qualified targets)
            // can pull in imports, so alias-only files stay import-free as before.
            val memberText = group.filterNot { it.isAlias }.joinToString("\n") { it.declaration }
            val used = imports.filter { (bound, _) -> bound == "*" || referenced(bound, memberText) }.values

            val out = buildString {
                if (pkg.isNotEmpty()) append("package $pkg\n\n")
                if (used.isNotEmpty()) append(used.joinToString("\n")).append("\n\n")
                append(declarations.joinToString("\n")).append("\n")
            }

            val fileName = if (pkg.isEmpty()) "Chameleons.kt" else "${pkg.replace('.', '/')}/Chameleons.kt"
            GeneratedFile(fileName, out)
        }
    }

    /** Extracts every chameleon carrier for [target] from a single file's [content]. */
    private fun parse(content: String, target: String): List<Carrier> {
        val pkg = PACKAGE.find(content)?.groupValues?.get(1).orEmpty()
        return CARRIER.findAll(content).mapNotNull { match ->
            val annotationRun = match.groupValues[1]
            val decl = match.groupValues[2].trim()

            val annotations = ANNOTATION.findAll(annotationRun).toList()
            val isChameleon = annotations.any { it.groupValues[1].substringAfterLast('.') == "Chameleon" }
            if (!isChameleon) return@mapNotNull null

            val platform = annotations.firstNotNullOfOrNull { platformOf(it) } ?: return@mapNotNull null
            if (platform != target) return@mapNotNull null

            resolve(decl, pkg, platform)
        }.toList()
    }

    /**
     * Resolves a carrier [decl] into the declaration to emit. A bare `val Name: Type` (no receiver,
     * no body) becomes a `typealias`; everything else — extension/computed properties and functions —
     * is emitted verbatim for the target.
     */
    private fun resolve(decl: String, pkg: String, platform: String): Carrier {
        if (decl.startsWith("val ")) {
            val match = VAL_CARRIER.matchEntire(decl)
            if (match != null) {
                val name = match.groupValues[1]
                val rest = match.groupValues[2].trim()
                val hasReceiver = VAL_HAS_RECEIVER.containsMatchIn(decl)
                val hasBody = '=' in rest || "get(" in rest
                if (!hasReceiver && !hasBody) {
                    return Carrier(platform, pkg, "typealias $name = $rest", isAlias = true)
                }
            }
        }
        return Carrier(platform, pkg, decl, isAlias = false)
    }

    /** Collects every `import` across [sources], keyed by the simple name (or alias) it binds. */
    private fun collectImports(sources: List<SourceFile>): Map<String, String> {
        val imports = LinkedHashMap<String, String>()
        sources.forEach { src ->
            IMPORT.findAll(src.content).forEach { match ->
                val path = match.groupValues[1]
                val alias = match.groupValues[2]
                val bound = when {
                    alias.isNotEmpty() -> alias
                    path.endsWith(".*") -> "*"
                    else -> path.substringAfterLast('.')
                }
                imports[bound] = if (alias.isEmpty()) "import $path" else "import $path as $alias"
            }
        }
        return imports
    }

    /** True if [name] appears as a whole-word token in [text]. */
    private fun referenced(name: String, text: String): Boolean =
        Regex("""\b${Regex.escape(name)}\b""").containsMatchIn(text)

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
