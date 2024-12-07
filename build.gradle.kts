plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.0"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "io.github.flyingpig525"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
    repositories {
        maven("https://repo.unnamed.team/repository/unnamed-public/")
        mavenCentral()
        maven("https://mvn.bladehunt.net/releases")
    }
}

dependencies {
    // Kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Minestom & minestom related
    implementation("net.minestom:minestom-snapshots:d760a60a5c")
    implementation("net.bladehunt:kotstom:0.3.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")

    // Noise lib
    implementation("de.articdive:jnoise-pipeline:4.1.0")

    // Creative resource pack manipulation
    implementation("team.unnamed:creative-api:1.7.3")
    implementation("team.unnamed:creative-serializer-minecraft:1.7.3")
    implementation("team.unnamed:creative-server:1.7.3")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "io.github.flyingpig525.MainKt"
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("")
    }
}