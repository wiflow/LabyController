rootProject.name = "labycontroller"

pluginManagement {
    val labyGradlePluginVersion = "0.5.9"
    buildscript {
        repositories {
            maven("https://dist.labymod.net/api/v1/maven/release/")
            maven("https://maven.neoforged.net/releases/")
            maven("https://maven.fabricmc.net/")
            gradlePluginPortal()
            mavenCentral()
        }

        dependencies {
            classpath("net.labymod.gradle", "common", labyGradlePluginVersion)
        }
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins.apply("net.labymod.labygradle.settings")

include(":api")
include(":core")
include(":game-runner")
