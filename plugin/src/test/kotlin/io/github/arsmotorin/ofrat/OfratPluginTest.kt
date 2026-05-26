package io.github.arsmotorin.ofrat

import io.github.arsmotorin.ofrat.compiler.PLATFORM_ONLY_FQ_NAME
import io.github.arsmotorin.ofrat.compiler.PlatformCommandLineProcessor
import io.github.arsmotorin.ofrat.compiler.PlatformPlugin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("DANGEROUS_CHARACTERS")
class OfratPluginTest {

    @Test fun `plugin id matches command line processor`() {
        assertEquals(PlatformCommandLineProcessor.PLUGIN_ID, PlatformPlugin().pluginId)
    }

    @Test fun `plugin supports K2`() {
        assert(PlatformPlugin().supportsK2)
    }

    @Test fun `platform option is registered`() {
        val opt = PlatformCommandLineProcessor().pluginOptions.firstOrNull { it.optionName == "platform" }
        assertNotNull(opt)
    }

    @Test fun `PLATFORM_ONLY_FQ_NAME matches annotation`() {
        assertEquals(PLATFORM_ONLY_FQ_NAME, PlatformOnly::class.qualifiedName)
    }

    @Test fun `FabricOnly carries PlatformOnly("fabric")`() {
        val p = FabricOnly::class.annotations.filterIsInstance<PlatformOnly>().firstOrNull()
        assertNotNull(p); assertEquals("fabric", p.platform)
    }

    @Test fun `PaperOnly carries PlatformOnly("paper")`() {
        val p = PaperOnly::class.annotations.filterIsInstance<PlatformOnly>().firstOrNull()
        assertNotNull(p); assertEquals("paper", p.platform)
    }

    @Test fun `NeoForgeOnly carries PlatformOnly("neoforge")`() {
        val p = NeoForgeOnly::class.annotations.filterIsInstance<PlatformOnly>().firstOrNull()
        assertNotNull(p); assertEquals("neoforge", p.platform)
    }
}
