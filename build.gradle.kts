subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    }

    apply(plugin = "maven-publish")

    extensions.configure<PublishingExtension> {
        repositories {
            mavenLocal()
        }
    }
}
