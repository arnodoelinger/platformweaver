plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    `maven-publish`
}

kotlin { jvmToolchain(21) }

base { archivesName.set("ofrat-annotations") }

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
            artifactId = "ofrat-annotations"
            from(components["java"])
            pom {
                name.set("OFRAT Annotations")
                description.set(
                    "Dependency for OFRAT plugin. @FabricOnly, @PaperOnly, @NeoForgeOnly and your own custom platform annotations."
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
