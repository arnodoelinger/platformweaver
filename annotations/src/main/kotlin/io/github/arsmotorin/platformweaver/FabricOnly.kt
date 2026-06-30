package io.github.arnodoelinger.platformweaver

/**
 * Marks a declaration as `Fabric`-specific.
 *
 * When compiling for any platform other than `"fabric"`, `Platform Weaver` removes every declaration
 * annotated with `@FabricOnly` from the compiler output.
 *
 * ## Usage
 *
 * ```kotlin
 * object Scheduler {
 *     // Compiled only into the Fabric JAR:
 *     @FabricOnly fun scheduleAsync(task: Runnable) {
 *         Thread(task, "my-async").also { it.isDaemon = true }.start()
 *     }
 *
 *     // Compiled only into the Paper JAR:
 *     @PaperOnly fun scheduleAsync(task: Runnable) {
 *         platform.scheduleAsync(task)
 *     }
 * }
 * ```
 *
 * ## Classpath note
 *
 * Because the Kotlin compiler resolves all types before the plugin strips declarations,
 * any `Fabric`-specific types referenced inside a `@FabricOnly` block must still be resolvable
 * at compile time. Add `Fabric API` as `compileOnly` alongside `Paper API` in shared modules.
 *
 * @see PlatformOnly
 * @see PaperOnly
 * @see NeoForgeOnly
 */
@PlatformOnly("fabric")
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@Retention(AnnotationRetention.SOURCE)
annotation class FabricOnly
