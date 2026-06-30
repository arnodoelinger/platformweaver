package io.github.arnodoelinger.platformweaver.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * `Platform Weaver` command-line processor.
 *
 * Parses the `platform` option passed via `-P plugin:io.github.arnodoelinger.platformweaver:platform=<value>`
 * and stores it in [io.github.arnodoelinger.platformweaver.compiler.PLATFORM_KEY] for [io.github.arnodoelinger.platformweaver.compiler.PlatformPlugin] to read during extension registration.
 */
@OptIn(ExperimentalCompilerApi::class)
class PlatformCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = PLUGIN_ID
    override val pluginOptions: Collection<AbstractCliOption> = listOf(_root_ide_package_.io.github.arnodoelinger.platformweaver.compiler.PLATFORM_OPTION)

    /** Process the command-line option and store the platform value in the compiler configuration. */
    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        if (option.optionName == _root_ide_package_.io.github.arnodoelinger.platformweaver.compiler.PLATFORM_OPTION.optionName) {
            configuration.put(_root_ide_package_.io.github.arnodoelinger.platformweaver.compiler.PLATFORM_KEY, value.trim().lowercase())
        }
    }

    companion object {
        const val PLUGIN_ID = "io.github.arnodoelinger.platformweaver"
    }
}
