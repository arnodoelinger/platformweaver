package io.github.arnodoelinger.platformweaver.gradle

/**
 * `Gradle DSL` extension for the `Platform Weaver`.
 *
 * ## Usage
 * ```kotlin
 * platformweaver {
 *     target = "paper"    // Or "fabric", "neoforge", or any custom platform name
 * }
 * ```
 */
abstract class PlatformWeaverExtension {
    /**
     * The target platform for this compilation unit.
     *
     * Declarations annotated for any other platform are stripped from the output.
     * Built-in values: `"fabric"`, `"paper"`, `"neoforge"`.
     * Custom values are supported via `@PlatformOnly("myplatform")`.
     */
    var target: String? = null

    /**
     * Path (relative to the project dir) to the directory holding `@Chameleon` carrier files.
     *
     * These files declare platform-resolved declarations — type aliases, extension / computed
     * properties, and functions — and are not compiled directly. The `Platform Weaver` `Gradle`
     * plugin parses them and generates the matching declaration per [target]. Defaults to
     * `src/main/chameleons`. Set to `null` to disable chameleon codegen entirely.
     */
    var chameleonsDir: String? = "src/main/chameleons"
}
