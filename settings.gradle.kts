plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
//    id("com.google.devtools.ksp") version "2.0.0-1.0.24"
}
rootProject.name = "MCOvertake"
include(":ksp")

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        google()
        repositories {
            maven("https://repo.unnamed.team/repository/unnamed-public/")
            mavenCentral()
            maven {
                name = "devOS"
                url = uri("https://mvn.devos.one/releases")
            }
        }
    }
}