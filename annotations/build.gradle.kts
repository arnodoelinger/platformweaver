plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    `maven-publish`
}

kotlin { jvmToolchain(21) }

base { archivesName.set("platformweaver-annotations") }

dependencies {
    implementation(kotlin("stdlib"))
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "platformweaver-annotations"
            from(components["java"])
            pom {
                name.set("Platform Weaver Annotations")
                description.set(
                    "Dependency for Platform Weaver plugin. @FabricOnly, @PaperOnly, @NeoForgeOnly and your own custom platform annotations."
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
                }
            }
        }
    }
}
