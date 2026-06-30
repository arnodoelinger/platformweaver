pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    }
    plugins {
        kotlin("jvm") version "2.4.0"
    }
}

rootProject.name = "platformweaver"
include(":annotations")
include(":plugin")
