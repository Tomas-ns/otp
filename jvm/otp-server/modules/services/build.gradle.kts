plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(23)) }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.6")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":repository"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("com.google.api-client:google-api-client:2.7.2")
    implementation("nz.ac.waikato.cms.weka:weka-stable:3.8.6")
    testImplementation(project(":domain"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
