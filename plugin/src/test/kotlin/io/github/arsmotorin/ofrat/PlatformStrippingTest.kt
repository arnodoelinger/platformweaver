@file:Suppress("SameParameterValue")

package io.github.arnodoelinger.ofrat

import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests that compile real Kotlin source with the `OFRAT` plugin
 * and verify via reflection that the correct declarations are stripped.
 *
 * ## Algorithm
 * 1. Writes a Kotlin source file to a temp directory.
 * 2. Invokes [K2JVMCompiler] with `-Xplugin=<ofrat-plugin.jar>` and the target platform.
 * 3. Loads the compiled class via [URLClassLoader].
 * 4. Asserts which methods are present / absent.
 */
class PlatformStrippingTest {

    private val pluginJar: String = System.getProperty("ofrat.plugin.jar")
        ?: error("System property 'ofrat.plugin.jar' is not set. Run tests via Gradle.")

    @Test fun `FabricOnly is stripped when platform is paper`() {
        val clazz = compile(
            platform = "paper", source = """
            import io.github.arnodoelinger.ofrat.FabricOnly
            import io.github.arnodoelinger.ofrat.PaperOnly
            object Target {
                @FabricOnly fun onFabric(): String = "fabric"
                @PaperOnly fun onPaper(): String = "paper"
                fun common(): String = "common"
            }
        """
        )
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue("onPaper" in methods, "onPaper must survive on paper")
        assertTrue("common" in methods, "common must survive on paper")
        assertFalse("onFabric" in methods, "onFabric must be stripped on paper")
    }

    @Test fun `PaperOnly is stripped when platform is fabric`() {
        val clazz = compile(
            platform = "fabric", source = """
            import io.github.arnodoelinger.ofrat.FabricOnly
            import io.github.arnodoelinger.ofrat.PaperOnly
            object Target {
                @FabricOnly fun onFabric(): String = "fabric"
                @PaperOnly fun onPaper(): String = "paper"
                fun common(): String = "common"
            }
        """
        )
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue("onFabric" in methods, "onFabric must survive on fabric")
        assertTrue("common" in methods, "common must survive on fabric")
        assertFalse("onPaper" in methods, "onPaper must be stripped on fabric")
    }

    @Test fun `NeoForgeOnly is stripped when platform is paper`() {
        val clazz = compile(
            platform = "paper", source = """
            import io.github.arnodoelinger.ofrat.NeoForgeOnly
            import io.github.arnodoelinger.ofrat.PaperOnly
            object Target {
                @NeoForgeOnly fun onNeoForge(): String = "neoforge"
                @PaperOnly fun onPaper(): String = "paper"
            }
        """
        )
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue("onPaper" in methods, "onPaper must survive")
        assertFalse("onNeoForge" in methods, "onNeoForge must be stripped")
    }

    @Test fun `unannotated declarations survive all platforms`() {
        listOf("fabric", "paper", "neoforge").forEach { platform ->
            val clazz = compile(
                platform = platform, source = """
                object Target {
                    fun common(): String = "common"
                    val value: Int = 42
                }
            """
            )
            assertTrue(
                "common" in clazz.declaredMethods.map { it.name },
                "common must survive on $platform",
            )
        }
    }

    @Test fun `FabricOnly class is stripped when platform is paper`() {
        val classFiles = compileToFiles(
            platform = "paper", source = """
            import io.github.arnodoelinger.ofrat.FabricOnly
            import io.github.arnodoelinger.ofrat.PaperOnly
            object Target {
                @FabricOnly class FabricHelper
                @PaperOnly class PaperHelper
            }
        """
        )
        assertTrue(classFiles.any { it.name == $$"Target$PaperHelper.class" }, "PaperHelper must survive")
        assertFalse(classFiles.any { it.name == $$"Target$FabricHelper.class" }, "FabricHelper must be stripped")
    }

    @Test fun `FabricOnly property is stripped when platform is paper`() {
        val clazz = compile(
            platform = "paper", source = """
            import io.github.arnodoelinger.ofrat.FabricOnly
            import io.github.arnodoelinger.ofrat.PaperOnly
            object Target {
                @FabricOnly val fabricConfig: String = "config/mod"
                @PaperOnly val paperConfig: String = "plugins/Mod"
            }
        """
        )
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue(methods.any { it.contains("paperConfig", ignoreCase = true) }, "paperConfig must survive")
        assertFalse(methods.any { it.contains("fabricConfig", ignoreCase = true) }, "fabricConfig must be stripped")
    }

    @Test fun `custom PlatformOnly annotation is kept on its own platform`() {
        val clazz = compile(
            platform = "spigot", source = """
            import io.github.arnodoelinger.ofrat.PlatformOnly
            @PlatformOnly("spigot")
            @Target(AnnotationTarget.FUNCTION)
            @Retention(AnnotationRetention.SOURCE)
            annotation class SpigotOnly

            object Subject {
                @SpigotOnly fun onSpigot(): String = "spigot"
                fun common(): String = "common"
            }
        """, className = "Subject"
        )
        val methods = clazz.declaredMethods.map { it.name }
        assertTrue("onSpigot" in methods, "onSpigot must survive on spigot")
        assertTrue("common" in methods, "common must survive on spigot")
    }

    @Test fun `custom PlatformOnly annotation is stripped on a different platform`() {
        val clazz = compile(
            platform = "paper", source = """
            import io.github.arnodoelinger.ofrat.PlatformOnly
            @PlatformOnly("spigot")
            @Target(AnnotationTarget.FUNCTION)
            @Retention(AnnotationRetention.SOURCE)
            annotation class SpigotOnly

            object Subject {
                @SpigotOnly fun onSpigot(): String = "spigot"
                fun common(): String = "common"
            }
        """, className = "Subject"
        )
        val methods = clazz.declaredMethods.map { it.name }
        assertFalse("onSpigot" in methods, "onSpigot must be stripped on paper")
        assertTrue("common" in methods, "common must survive")
    }

    /**
     * Compiles [source] with the `OFRAT` plugin targeting [platform] and returns
     * the loaded class named [className] from the resulting bytecode.
     */
    private fun compile(platform: String, source: String, className: String = "Target"): Class<*> {
        val outputDir = runCompiler(platform, source)
        val loader = URLClassLoader(
            arrayOf(outputDir.toURI().toURL()),
            Thread.currentThread().contextClassLoader,
        )
        return loader.loadClass(className)
    }

    /**
     * Compiles [source] and returns all `.class` files produced in the output directory.
     * Useful for checking nested class presence without triggering class loading.
     */
    private fun compileToFiles(platform: String, source: String): List<File> {
        val outputDir = runCompiler(platform, source)
        return outputDir.walkTopDown().filter { it.extension == "class" }.toList()
    }

    /**
     * Compiles [source] and returns the output directory.
     *
     * @param platform the target platform for compilation.
     * @param source the source code to compile.
     * @return the output directory containing compiled class files.
     */
    private fun runCompiler(platform: String, source: String): File {
        val tempDir = Files.createTempDirectory("ofrat-integration").toFile()
        val sourceFile = File(tempDir, "Test.kt").also { it.writeText(source.trimIndent()) }
        val outputDir = File(tempDir, "out").also { it.mkdirs() }

        val exitCode = K2JVMCompiler().exec(
            System.err,
            sourceFile.absolutePath,
            "-d", outputDir.absolutePath,
            "-cp", System.getProperty("java.class.path"),
            "-Xplugin=$pluginJar",
            "-P", "plugin:io.github.arnodoelinger.ofrat:platform=$platform",
            "-no-stdlib",
            "-no-reflect",
        )
        check(exitCode == ExitCode.OK) {
            "Kotlin compilation failed (exit=$exitCode) for platform=$platform"
        }
        return outputDir
    }
}
