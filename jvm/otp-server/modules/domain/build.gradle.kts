plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.jpa") version "2.1.20"
    kotlin("plugin.allopen") version "2.1.20"
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(23)) }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
}
