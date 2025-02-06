plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "io.github.flyingpig525.ksp"
version = "0.4"

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.10-1.0.29")
}

kotlin {
    jvmToolchain(21)
}