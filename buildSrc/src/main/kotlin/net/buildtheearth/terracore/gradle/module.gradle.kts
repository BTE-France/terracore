package net.buildtheearth.terracore.gradle

import io.freefair.gradle.plugins.lombok.tasks.Delombok

plugins {
    id("java-library")
    id("eclipse")
    id("maven-publish")
    id("io.freefair.lombok")
}

group = "net.buildtheearth.terracore"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.daporkchop.net/") // Required for leveldb
}

dependencies {
    // Annotations
    compileOnly("org.jetbrains:annotations:26.0.2")

    // Tests
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("log4j.configurationFile", "log4j2.xml")
    testLogging {
        events("passed", "skipped", "failed")
    }
}

publishing {
    repositories {
        maven {
            name = "smylerSnapshots"
            url = uri("https://maven.smyler.net/snapshots")
            credentials {
                username = System.getenv("MAVEN_NAME")
                password = System.getenv("MAVEN_SECRET")
            }
        }
        maven {
            name = "smylerReleases"
            url = uri("https://maven.smyler.net/releases")
            credentials {
                username = System.getenv("MAVEN_NAME")
                password = System.getenv("MAVEN_SECRET")
            }
        }
    }
}

