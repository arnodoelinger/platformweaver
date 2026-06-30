package io.github.arnodoelinger.ofrat.gradle

/**
 * `Gradle DSL` extension for the `OFRAT`.
 *
 * ## Usage
 * ```kotlin
 * ofrat {
 *     target = "paper"    // Or "fabric", "neoforge", or any custom platform name
 * }
 * ```
 */
abstract class OfratExtension {
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
     * These files declare platform-resolved type aliases and are not compiled directly. The
     * `OFRAT` `Gradle` plugin parses them and generates a real `typealias` per [target]. Defaults to
     * `src/main/chameleons`. Set to `null` to disable chameleon codegen entirely.
     */
    var chameleonsDir: String? = "src/main/chameleons"
}
