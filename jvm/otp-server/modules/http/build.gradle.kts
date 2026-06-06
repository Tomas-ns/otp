plugins {
    kotlin("jvm") version "2.1.20"
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(23)) }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":services"))
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.0")
}