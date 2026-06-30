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

    @Test fun `extension property carrier is emitted verbatim for the target only`() {
        val accessors = SourceFile(
            """
            package com.example

            @PaperOnly @Chameleon val PlatPlayer.platUuid: java.util.UUID get() = uniqueId
            @FabricOnly @Chameleon val PlatPlayer.platUuid: java.util.UUID get() = uuid
            """.trimIndent()
        )

        val paper = ChameleonGenerator.generate(listOf(accessors), "paper")[0].content
        assertTrue("val PlatPlayer.platUuid: java.util.UUID get() = uniqueId" in paper)
        assertTrue("typealias" !in paper, "a property carrier must not become a typealias")
        assertTrue("uuid" !in paper.substringAfter("get() = "), "fabric accessor must not leak into paper")

        val fabric = ChameleonGenerator.generate(listOf(accessors), "fabric")[0].content
        assertTrue("val PlatPlayer.platUuid: java.util.UUID get() = uuid" in fabric)
        assertTrue("uniqueId" !in fabric, "paper accessor must not leak into fabric")
    }

    @Test fun `function carrier is emitted verbatim`() {
        val fns = SourceFile(
            """
            @PaperOnly @Chameleon fun PlatPlayer.platWorldName(): String = world.name
            @FabricOnly @Chameleon fun PlatPlayer.platWorldName(): String = level().dimension().location().toString()
            """.trimIndent()
        )
        assertTrue(
            "fun PlatPlayer.platWorldName(): String = world.name"
                in ChameleonGenerator.generate(listOf(fns), "paper")[0].content
        )
        assertTrue(
            "fun PlatPlayer.platWorldName(): String = level().dimension().location().toString()"
                in ChameleonGenerator.generate(listOf(fns), "fabric")[0].content
        )
    }

    @Test fun `imports used by a member body are carried through`() {
        val withImports = SourceFile(
            """
            package com.example

            import com.example.server.Main
            import com.example.server.Server
            import org.bukkit.entity.Player

            @PaperOnly @Chameleon val platConfig: com.example.Config get() = Main.config
            @FabricOnly @Chameleon val platConfig: com.example.Config get() = Server.config
            """.trimIndent()
        )

        val paper = ChameleonGenerator.generate(listOf(withImports), "paper")[0].content
        assertTrue("import com.example.server.Main" in paper, "the referenced import must be carried")
        assertTrue("import com.example.server.Server" !in paper, "the unreferenced (fabric) import must be dropped")
        assertTrue("import org.bukkit.entity.Player" !in paper, "an unused import must be dropped")
    }

    @Test fun `type alias files stay import-free`() {
        val mixed = SourceFile(
            """
            package com.example

            import org.bukkit.entity.Player

            @PaperOnly @Chameleon val PlatPlayer: org.bukkit.entity.Player
            """.trimIndent()
        )
        val paper = ChameleonGenerator.generate(listOf(mixed), "paper")[0].content
        assertTrue("typealias PlatPlayer = org.bukkit.entity.Player" in paper)
        assertTrue("import" !in paper, "a fully-qualified alias must not pull in an import")
    }
}
