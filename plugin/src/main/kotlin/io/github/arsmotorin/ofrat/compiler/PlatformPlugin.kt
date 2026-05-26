package io.github.arsmotorin.ofrat.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * `OFRAT` compiler plugin registrar.
 *
 * Registered via `META-INF/services`; picks up the `platform` option set by the `Gradle`
 * plugin and installs [PlatformIrTransformer] when a non-blank target is configured.
 */
@OptIn(ExperimentalCompilerApi::class)
class PlatformPlugin : CompilerPluginRegistrar() {
    override val pluginId: String = PlatformCommandLineProcessor.PLUGIN_ID
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val platform = configuration[PLATFORM_KEY]
        if (platform.isNullOrBlank()) return
        IrGenerationExtension.registerExtension(PlatformIrTransformer(platform))
    }
}
