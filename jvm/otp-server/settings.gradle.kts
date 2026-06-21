pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.1.20"
        kotlin("plugin.spring") version "2.1.20"
        kotlin("plugin.jpa") version "2.1.20"
        kotlin("plugin.allopen") version "2.1.20"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "otp-server"

dependencyResolutionManagement {
    // This ensures ALL modules use these repositories
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral() // Where Kotlin 2.1.20 and Spring live
    }
}

include("host")
project(":host").projectDir = file("modules/host")

include("domain")
project(":domain").projectDir = file("modules/domain")

include("http")
project(":http").projectDir = file("modules/http")

include("services")
project(":services").projectDir = file("modules/services")

include("repository")
project(":repository").projectDir = file("modules/repository")
