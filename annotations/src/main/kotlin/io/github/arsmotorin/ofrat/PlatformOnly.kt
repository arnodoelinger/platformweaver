package io.github.arsmotorin.ofrat

/**
 * Meta-annotation that marks an annotation class as targeting a specific platform.
 *
 * `OFRAT` reads this annotation to decide which declarations
 * to strip at compile time. When a build targets `"paper"`, every declaration annotated
 * with an annotation whose `@PlatformOnly` [platform] value is not `"paper"` is
 * removed from the compiler output.
 *
 * ## Defining a custom platform annotation
 *
 * ```kotlin
 * @PlatformOnly("spigot")
 * @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
 * @Retention(AnnotationRetention.SOURCE)
 * annotation class SpigotOnly
 * ```
 *
 * After this definition, `@SpigotOnly` declarations are automatically stripped from all
 * non-`Spigot` compilations — no changes to the plugin required.
 *
 * @param platform lowercase platform identifier (e.g. `"fabric"`, `"paper"`, `"neoforge"`).
 *   Must match the `ofrat { target = "..." }` value in the consuming module's build script.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PlatformOnly(val platform: String)
