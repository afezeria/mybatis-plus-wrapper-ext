import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "1.9.10" apply false
    id("org.jetbrains.kotlinx.kover") version "0.7.3" apply false
}

val ossrhUsername: String by project
val ossrhPassword: String by project

subprojects {
    group = "io.github.afezeria"
    version = "2.0.0"

    apply {
        plugin("kotlin")
        plugin("maven-publish")
        plugin("signing")
        plugin("org.jetbrains.kotlinx.kover")
    }

    repositories {
        mavenCentral()
    }

    configure<KotlinJvmProjectExtension> {
        jvmToolchain(8)
    }

    dependencies {
        val implementation by configurations
        val testImplementation by configurations
        implementation(kotlin("stdlib"))

        val kotestVersion = "5.7.2"
        testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        testImplementation(kotlin("test"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }


    if (project.name != "example") {

        dependencies {
            val compileOnly by configurations
            val testImplementation by configurations

            compileOnly("com.baomidou:mybatis-plus-core:3.5.3.2")
            testImplementation("com.baomidou:mybatis-plus-core:3.5.3.2")
        }

        val sourcesJar by tasks.creating(Jar::class) {
            archiveClassifier.set("sources")
            from(project.the<SourceSetContainer>()["main"].allSource)
        }
        val javadocJar by tasks.creating(Jar::class) {
            archiveClassifier.set("javadoc")
        }
        tasks.withType<Jar> {
            archiveBaseName = rootProject.name + "-" + project.name
        }

        artifacts {
            add("archives", sourcesJar)
            add("archives", javadocJar)
        }

        configure<PublishingExtension> {
            repositories {
                maven {
                    if (project.version.toString().endsWith("-SNAPSHOT")) {
                        setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                    } else {
                        setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    }
                    credentials {
                        username = ossrhUsername
                        password = ossrhPassword
                    }
                }
            }


            publications {
                register<MavenPublication>("mavenJava") {
                    from(components["java"])
                    artifact(sourcesJar)
                    artifact(javadocJar)

                    groupId = project.group.toString()
                    artifactId = rootProject.name + "-" + project.name
                    version = project.version.toString()

                    pom {
                        name.set("${project.group}:${project.name}")
                        description.set("An extension library for the wrapper API of Mybatis-Plus written in Kotlin.")
                        url.set("https://github.com/afezeria/mybatis-plus-wrapper-ext")
                        licenses {
                            license {
                                name.set("The Apache Software License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        scm {
                            url.set("https://github.com/afezeria/freedao")
                            connection.set("scm:git:https://github.com/afezeria/mybatis-plus-wrapper-ext.git")
                            developerConnection.set("scm:git:ssh://git@github.com/afezeria/mybatis-plus-wrapper-ext.git")
                        }
                        developers {
                            developer {
                                id.set("afezeria")
                                name.set("afezeria")
                                email.set("zodal@outlook.com")
                            }
                        }
                    }
                }
            }
        }

        configure<SigningExtension> {
            sign(the<PublishingExtension>().publications["mavenJava"])
        }
    }
}
