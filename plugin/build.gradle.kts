plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    alias(libs.plugins.maven.publish)
}

kotlin { jvmToolchain(21) }

base { archivesName.set("platformweaver-plugin") }

dependencies {
    implementation(project(":annotations"))
    compileOnly(libs.kotlin.compiler.embeddable)
    compileOnly(gradleApi())
    compileOnly(libs.kotlin.gradle.plugin)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(kotlin("reflect"))
}

tasks.test {
    dependsOn(tasks.jar)
    systemProperty("platformweaver.plugin.jar", tasks.jar.get().archiveFile.get().asFile.absolutePath)
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    coordinates(group.toString(), "platformweaver-plugin", version.toString())

    pom {
        name.set("Platform Weaver")
        description.set(
            "Compiler plugin for Minecraft mod / plugin developers. Annotate by platform, get a clean JAR per target. " +
                    "Strips platform-specific declarations from the IR tree at compile time based on meta-annotations (@FabricOnly, @PaperOnly, @NeoForgeOnly, custom). " +
                    "Without stubs, wrappers and runtime overhead."
        )
        url.set("https://github.com/arnodoelinger/PlatformWeaver")
        licenses {
            license {
                name.set("LGPL-3.0 License")
                url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
            }
        }
        developers {
            developer {
                id.set("arnodoelinger")
                name.set("Arno Dölinger")
            }
        }
        scm {
            url.set("https://github.com/arnodoelinger/PlatformWeaver")
            connection.set("scm:git:https://github.com/arnodoelinger/PlatformWeaver.git")
            developerConnection.set("scm:git:ssh://git@github.com/arnodoelinger/PlatformWeaver.git")
        }
    }
}
