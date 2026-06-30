package io.github.arnodoelinger.platformweaver

/**
 * Marks a declaration as Paper-specific (also covers `Folia`).
 *
 * When compiling for any platform other than `"paper"`, `Platform Weaver` removes every declaration
 * annotated with `@PaperOnly` from the compiler output.
 *
 * ## Usage
 *
 * ```kotlin
 * object Scheduler {
 *     // Compiled only into the Paper JAR:
 *     @PaperOnly fun scheduleAsync(task: Runnable) {
 *         Bukkit.getScheduler().runTaskAsync(plugin, task)
 *     }
 *
 *     // Compiled only into the Fabric JAR:
 *     @FabricOnly fun scheduleAsync(task: Runnable) {
 *         Thread(task, "my-async").also { it.isDaemon = true }.start()
 *     }
 * }
 * ```
 *
 * ## Classpath note
 *
 * Because the Kotlin compiler resolves all types before the plugin strips declarations,
 * any Paper-specific types referenced inside a `@PaperOnly` block must still be resolvable
 * at compile time. Add `Paper API` as `compileOnly` alongside `Fabric API` in shared modules.
 *
 * @see PlatformOnly
 * @see FabricOnly
 * @see NeoForgeOnly
 */
@PlatformOnly("paper")
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@Retention(AnnotationRetention.SOURCE)
annotation class PaperOnly
