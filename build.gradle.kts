import java.net.URI

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "io.github.flyingpig525"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    repositories {
        maven("https://mvn.bladehunt.net/releases")
    }
}

dependencies {
    implementation("net.minestom:minestom-snapshots:6fc64e3a5d")
    implementation("net.bladehunt:kotstom:0.3.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}