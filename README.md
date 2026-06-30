[![JitPack](https://jitpack.io/v/arnodoelinger/platformweaver.svg)](https://jitpack.io/#arnodoelinger/platformweaver)
[![License](https://img.shields.io/github/license/arnodoelinger/PlatformWeaver)](https://github.com/arnodoelinger/PlatformWeaver/blob/master/LICENSE)
[![Discord](http://img.shields.io/discord/1456716690879676501?label=Discord&style=flat&logo=discord)](https://discord.gg/uwMMZ2KWk6)

<div align="center"> <img src="https://i.imgur.com/IqxbK2y.png" alt="Platform Weaver"> </div>

<div align="center">Annotate by platform, get a clean JAR per target.</div>

> [!WARNING]
> Platform Weaver is an experimental project and API can be changed in the future. You can use it, but it's not recommended.

## The situation

You're writing a Minecraft server plugin — and it has to run on both Fabric and Paper.
The logic is identical. The same storage format, same commands, same packet handling, etc.
But Fabric says `ServerLevel`, Paper says `World`. Fabric says `BlockPos`, Paper says `Location`.

So you end up with two codebases that do the same thing. **Platform Weaver breaks that loop.**

## How it works

Annotate declarations by platform. The compiler plugin removes the ones that don't belong — before a single byte is written to disk.

```kotlin
object Scheduler {
    @FabricOnly fun scheduleAsync(task: Runnable) {
        Thread(task, "async-worker").also { it.isDaemon = true }.start()
    }

    @PaperOnly fun scheduleAsync(task: Runnable) {
        Bukkit.getScheduler().runTaskAsync(plugin, task)
    }
}
```

Compile for Paper? The `@FabricOnly` block is gone — it never made it to bytecode. No runtime overhead, and it's the same file for both platforms.

And when the platforms differ only in a type name or an accessor, [`@Chameleon`](https://github.com/arnodoelinger/PlatformWeaver/wiki/Chameleon) merges them so the shared logic is written exactly once.

## Documentation

The full guide lives in the [wiki](https://github.com/arnodoelinger/PlatformWeaver/wiki).

## Donate us

Platform Weaver is developed with passion and dedication by me and my friends. If you enjoy our mod and want to support
further development, consider making a [donation](https://www.paypal.com/paypalme/frogdream) or [buying us a coffee](https://ko-fi.com/arnodoelinger).
