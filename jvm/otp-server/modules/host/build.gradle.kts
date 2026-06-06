plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(23)) }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":services"))
    implementation(project(":http"))

    implementation("org.springframework.boot:spring-boot-starter")
}