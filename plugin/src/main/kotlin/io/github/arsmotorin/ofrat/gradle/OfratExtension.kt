package io.github.arsmotorin.ofrat.gradle

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
}
