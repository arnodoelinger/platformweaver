package io.github.arnodoelinger.platformweaver.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.config.CompilerConfigurationKey

/** Compiler configuration key that holds the target platform string (e.g. `"paper"`). */
internal val PLATFORM_KEY = CompilerConfigurationKey.create<String>("platformweaver.platform.target")

/**
 * CLI option accepted by [PlatformCommandLineProcessor].
 *
 * Passed via `-P plugin:io.github.arnodoelinger.platformweaver:platform=<value>`.
 */
internal val PLATFORM_OPTION: AbstractCliOption = CliOption(
    optionName = "platform",
    valueDescription = "<fabric|paper|neoforge|...>",
    description = "Target platform. Declarations annotated for other platforms are stripped.",
    required = false,
    allowMultipleOccurrences = false,
)

/** Fully-qualified name of [io.github.arnodoelinger.platformweaver.PlatformOnly], used for IR lookup. */
internal const val PLATFORM_ONLY_FQ_NAME = "io.github.arnodoelinger.platformweaver.PlatformOnly"
