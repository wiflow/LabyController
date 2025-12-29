plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

val sdl3Target: String by project
val sdl34jBuild: String by project

allprojects {
    repositories {
        maven("https://maven.isxander.dev/releases") {
            name = "Xander Maven"
        }
        mavenCentral()
    }
}

group = "net.labymod.addons"
version = providers.environmentVariable("VERSION").getOrElse("1.0.0")

labyMod {
    defaultPackageName = "net.labymod.addons.labycontroller"

    minecraft {
        registerVersion(versions.toTypedArray()) {
        }
    }

    addonInfo {
        namespace = "labycontroller"
        displayName = "LabyController"
        author = "WiFlow"
        description = "Full controller support for Minecraft - play with your gamepad!"
        minecraftVersion = "*"
        version = rootProject.version.toString()
    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")

    group = rootProject.group
    version = rootProject.version
}
