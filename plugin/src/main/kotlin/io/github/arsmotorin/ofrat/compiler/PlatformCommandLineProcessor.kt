package io.github.arnodoelinger.ofrat.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * `OFRAT` command-line processor.
 *
 * Parses the `platform` option passed via `-P plugin:io.github.arnodoelinger.ofrat:platform=<value>`
 * and stores it in [PLATFORM_KEY] for [PlatformPlugin] to read during extension registration.
 */
@OptIn(ExperimentalCompilerApi::class)
class PlatformCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = PLUGIN_ID
    override val pluginOptions: Collection<AbstractCliOption> = listOf(PLATFORM_OPTION)

    /** Process the command-line option and store the platform value in the compiler configuration. */
    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        if (option.optionName == PLATFORM_OPTION.optionName) {
            configuration.put(PLATFORM_KEY, value.trim().lowercase())
        }
    }

    companion object {
        const val PLUGIN_ID = "io.github.arnodoelinger.ofrat"
    }
}
