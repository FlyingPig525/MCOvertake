import java.net.URI

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
        maven("https://mvn.bladehunt.net/releases")
        maven("https://repo.unnamed.team/repository/unnamed-public/")
    }
}

val hephaestusVersion = "0.6.0-SNAPSHOT"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("net.minestom:minestom-snapshots:bb7acc2e77")
    implementation("net.bladehunt:kotstom:0.3.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")

    implementation("team.unnamed:hephaestus-api:$hephaestusVersion")
    implementation("team.unnamed:hephaestus-reader-blockbench:$hephaestusVersion")
    implementation("team.unnamed:hephaestus-runtime-minestom:0.4.1-SNAPSHOT")
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