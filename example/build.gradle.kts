plugins {
    id("org.springframework.boot") version "2.7.16"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"

    kotlin("plugin.spring") version "1.9.10"
    id("com.google.devtools.ksp") version "1.9.10-1.0.13"
}

dependencies {
    implementation(project(":runtime"))
    ksp(project(":processor"))
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.3.2")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

}

