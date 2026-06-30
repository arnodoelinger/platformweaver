package io.github.arnodoelinger.platformweaver

/**
 * Declares a platform-resolved declaration. One name that becomes a different concrete
 * definition per target platform, like a chameleon changing color to match its surroundings. :)
 *
 * Place `@Chameleon` on a carrier together with a platform annotation ([io.github.arnodoelinger.platformweaver.FabricOnly], [io.github.arnodoelinger.platformweaver.PaperOnly],
 * [io.github.arnodoelinger.platformweaver.NeoForgeOnly], or any [io.github.arnodoelinger.platformweaver.PlatformOnly]). `Platform Weaver`'s `Gradle` codegen reads the carriers
 * and emits the matching declaration for the active platform only, so you never write the variants
 * by hand, and you never get a redeclaration clash from them. Three carrier shapes are supported:
 *
 * - Type alias — a bare `val Name: Type` becomes `typealias Name = Type`
 * - Property — a `val [Receiver.]name: T` with a body is emitted as-is
 * - Function — a `fun [Receiver.]name(...): T = ...` is emitted as-is
 *
 * ## The duplication it removes
 *
 * A type alias only erases *type-name* divergence. The real cost in shared platform code is
 * *accessor* and behaviour divergence — the same method written twice only because one platform
 * spells it `player.uniqueId` and the other `player.uuid`. Chameleon members absorb exactly that:
 *
 * ```kotlin
 * // src/main/chameleons/Platform.kt — read by codegen, never compiled directly:
 * @PaperOnly  @Chameleon val PlatPlayer: org.bukkit.entity.Player
 * @FabricOnly @Chameleon val PlatPlayer: net.minecraft.server.level.ServerPlayer
 *
 * @PaperOnly  @Chameleon val PlatPlayer.platUuid: java.util.UUID get() = uniqueId
 * @FabricOnly @Chameleon val PlatPlayer.platUuid: java.util.UUID get() = uuid
 * ```
 *
 * The shared source then collapses a `@PaperOnly` / `@FabricOnly` pair into a single body:
 *
 * ```kotlin
 * // Written once, instead of one variant per platform:
 * fun resetSelection(player: PlatPlayer) = resetSelection(player.platUuid)
 * ```
 *
 * ## Carriers are metadata only
 *
 * Kotlin's grammar requires every annotation to sit on a declaration, so the `typealias` / `fun`/`val`
 * keyword cannot be dropped outright. Carriers are never compiled, only parsed — each must be a
 * single line (expression-bodied), and member bodies may use any symbol imported in the carrier
 * file, which codegen carries through when the emitted declaration references it.
 *
 * ## Classpath note
 *
 * Codegen resolves carriers textually, so a non-target platform's type need not be on the classpath.
 *
 * @see io.github.arnodoelinger.platformweaver.PlatformOnly
 * @see io.github.arnodoelinger.platformweaver.FabricOnly
 * @see io.github.arnodoelinger.platformweaver.PaperOnly
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Chameleon
