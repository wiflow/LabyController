import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

val sdl3Target: String by project
val sdl34jBuild: String by project

// Configuration for dependencies to be bundled into the JAR
val bundle: Configuration by configurations.creating {
    // Don't include transitive deps - LabyMod already has JNA
    isTransitive = false
}

dependencies {
    labyProcessor()
    api(project(":api"))

    // Include core LabyMod API with Minecraft/LWJGL dependencies
    labyApi("core")

    // SDL3 Java bindings (JNA is provided by LabyMod)
    api("dev.isxander:libsdl4j:$sdl3Target-$sdl34jBuild")
    bundle("dev.isxander:libsdl4j:$sdl3Target-$sdl34jBuild")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}

// SDL3 native library configuration
data class NativeTarget(
    val classifier: String,
    val fileExtension: String,
    val jnaPrefix: String,
    val fileName: String,
    val configurationName: String,
)

val nativeTargets = listOf(
    NativeTarget(classifier = "linux-aarch64", fileExtension = "so", jnaPrefix = "linux-aarch64/", fileName = "libSDL3", configurationName = "sdlNativeLinuxAarch64"),
    NativeTarget(classifier = "linux-x86_64", fileExtension = "so", jnaPrefix = "linux-x86-64/", fileName = "libSDL3", configurationName = "sdlNativeLinuxX86_64"),
    NativeTarget(classifier = "macos-aarch64", fileExtension = "dylib", jnaPrefix = "darwin-aarch64/", fileName = "libSDL3", configurationName = "sdlNativeMacArm"),
    NativeTarget(classifier = "macos-x86_64", fileExtension = "dylib", jnaPrefix = "darwin-x86-64/", fileName = "libSDL3", configurationName = "sdlNativeMacIntel"),
    NativeTarget(classifier = "windows-x86_64", fileExtension = "dll", jnaPrefix = "win32-x86-64/", fileName = "SDL3", configurationName = "sdlNativeWinX86_64"),
)

// Create configurations for each native target
val nativeConfigurations = nativeTargets.associate { target ->
    target.configurationName to configurations.create(target.configurationName)
}

// Add native dependencies
nativeTargets.forEach { target ->
    dependencies {
        nativeConfigurations[target.configurationName]!!("dev.isxander:libsdl4j-natives:$sdl3Target:${target.classifier}@${target.fileExtension}")
    }
}

val prepareNatives = tasks.register<Sync>("prepareSdlNatives") {
    group = "labycontroller"

    into(layout.buildDirectory.dir("generated-resources/sdl-natives"))

    nativeTargets.forEach { target ->
        from(configurations.named(target.configurationName)) {
            into(target.jnaPrefix)
            rename { "${target.fileName}.${target.fileExtension}" }
        }
    }
}

// Add generated resources to source sets
sourceSets {
    main {
        resources.srcDir(prepareNatives.map { it.destinationDir })
    }
}

tasks.processResources {
    dependsOn(prepareNatives)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Include bundled dependencies in the JAR
tasks.named<Jar>("jar") {
    dependsOn(prepareNatives)

    // Include all bundled dependency classes
    from({
        bundle.map { if (it.isDirectory) it else zipTree(it) }
    }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
