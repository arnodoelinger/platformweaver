package io.github.arnodoelinger.platformweaver

/**
 * Declares a platform-resolved type alias. One name that becomes a different concrete type
 * per target platform, like a chameleon changing color to match its surroundings. :)
 *
 * Place `@Chameleon` on a `val` carrier together with a platform annotation
 * ([FabricOnly], [PaperOnly], [NeoForgeOnly], or any [PlatformOnly]). The carrier's name
 * becomes the alias name and its declared type becomes the alias target. `Platform Weaver`'s `Gradle`
 * codegen reads these carriers and emits a real `typealias` for the active platform only, so you
 * never write the `typealias` keyword, and you never get a redeclaration clash from the variants.
 *
 * ## Usage
 *
 * ```kotlin
 * // src/main/chameleons/Platform.kt that read by codegen, never compiled directly:
 * @PaperOnly @Chameleon val PlatPlayer: org.bukkit.entity.Player
 * @FabricOnly @Chameleon val PlatPlayer: net.minecraft.server.level.ServerPlayer
 * ```
 *
 * After codegen, the shared source can use the single name:
 *
 * ```kotlin
 * // Written once instead of a @PaperOnly / @FabricOnly pair:
 * fun getVersion(player: PlatPlayer): Semver? = versions[player.uid]
 * ```
 *
 * ## Why a `val` carrier
 *
 * Kotlin's grammar requires every annotation to sit on a declaration, so the `typealias` keyword
 * cannot be dropped outright. `val` is the lightest legal carrier and unlike `class` / `object`
 * supertype forms, it works for `final` platform types (e.g. `ServerPlayer`). The carrier is only
 * metadata: it is never compiled, only parsed.
 *
 * ## Classpath note
 *
 * Unlike platform-stripped code, string resolves chameleon carriers, so the non-target
 * platform's type need not be on the classpath at all.
 *
 * @see PlatformOnly
 * @see FabricOnly
 * @see PaperOnly
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Chameleon
