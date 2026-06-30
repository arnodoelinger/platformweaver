package io.github.arnodoelinger.platformweaver

import io.github.arnodoelinger.platformweaver.gradle.ChameleonGenerator
import io.github.arnodoelinger.platformweaver.gradle.ChameleonGenerator.SourceFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for the `@Chameleon` codegen. Verifies that platform-resolved type aliases are
 * emitted for the active platform only, regardless of annotation order or package.
 */
class ChameleonGeneratorTest {

    private val carriers = SourceFile(
        """
        package com.example

        @PaperOnly @Chameleon val PlatPlayer: org.bukkit.entity.Player
        @FabricOnly @Chameleon val PlatPlayer: net.minecraft.server.level.ServerPlayer
        """.trimIndent()
    )

    @Test fun `emits the paper target for a carrier pair`() {
        val generated = ChameleonGenerator.generate(listOf(carriers), "paper")
        assertEquals(1, generated.size)
        assertEquals("com/example/Chameleons.kt", generated[0].relativePath)
        assertTrue("package com.example" in generated[0].content)
        assertTrue("typealias PlatPlayer = org.bukkit.entity.Player" in generated[0].content)
        assertTrue("ServerPlayer" !in generated[0].content, "fabric target must not leak into paper output")
    }

    @Test fun `emits the fabric target for a carrier pair`() {
        val generated = ChameleonGenerator.generate(listOf(carriers), "fabric")
        assertTrue("typealias PlatPlayer = net.minecraft.server.level.ServerPlayer" in generated[0].content)
        assertTrue("org.bukkit" !in generated[0].content, "paper target must not leak into fabric output")
    }

    @Test fun `annotation order does not matter`() {
        val reordered = SourceFile(
            """
            @Chameleon @PaperOnly val PlatPos: org.bukkit.Location
            """.trimIndent()
        )
        val generated = ChameleonGenerator.generate(listOf(reordered), "paper")
        assertTrue("typealias PlatPos = org.bukkit.Location" in generated[0].content)
    }

    @Test fun `carrier without Chameleon is ignored`() {
        val notAChameleon = SourceFile(
            """
            @PaperOnly val ordinaryProperty: String
            """.trimIndent()
        )
        val generated = ChameleonGenerator.generate(listOf(notAChameleon), "paper")
        assertTrue(generated.isEmpty(), "a platform-annotated val without @Chameleon is not a carrier")
    }

    @Test fun `custom PlatformOnly value resolves the platform`() {
        val custom = SourceFile(
            """
            @PlatformOnly("spigot") @Chameleon val PlatServer: org.bukkit.Server
            """.trimIndent()
        )
        assertTrue(ChameleonGenerator.generate(listOf(custom), "spigot").isNotEmpty())
        assertTrue(ChameleonGenerator.generate(listOf(custom), "paper").isEmpty())
    }

    @Test fun `nullable and generic targets are preserved`() {
        val fancy = SourceFile(
            """
            @PaperOnly @Chameleon val PlatList: kotlin.collections.List<org.bukkit.entity.Player>?
            """.trimIndent()
        )
        val generated = ChameleonGenerator.generate(listOf(fancy), "paper")
        assertTrue(
            "typealias PlatList = kotlin.collections.List<org.bukkit.entity.Player>?" in generated[0].content,
            "generic + nullable target must survive verbatim",
        )
    }
}
