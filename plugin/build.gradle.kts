plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    `maven-publish`
}

kotlin { jvmToolchain(21) }

base { archivesName.set("ofrat-plugin") }

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
    systemProperty("ofrat.plugin.jar", tasks.jar.get().archiveFile.get().asFile.absolutePath)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "ofrat-plugin"
            from(components["java"])
            pom {
                name.set("OFRAT")
                description.set(
                    "Kotlin compiler plugin for Minecraft mod / plugin developers. Annotate by platform, get a clean JAR per target. " +
                            "Strips platform-specific declarations from the IR tree at compile time based on meta-annotations (@FabricOnly, @PaperOnly, @NeoForgeOnly, custom). " +
                            "Without stubs, wrappers and runtime overhead."
                )
                url.set("https://github.com/arsmotorin/OFRAT")
                licenses {
                    license {
                        name.set("LGPL-3.0 License")
                        url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
                    }
                }
                developers {
                    developer {
                        id.set("arsmotorin")
                        name.set("Arsenii Motorin")
                    }
                }
                scm {
                    url.set("https://github.com/arsmotorin/OFRAT")
                }
            }
        }
    }
}
