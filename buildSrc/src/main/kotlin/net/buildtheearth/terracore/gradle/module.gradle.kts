package net.buildtheearth.terracore.gradle

import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("java-library")
    id("eclipse")
    id("maven-publish")
    id("io.freefair.lombok")
}

// Precompiled script plugins don't get type-safe catalog accessors, so look the catalog up at runtime
val libs = the<VersionCatalogsExtension>().named("libs")

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
    compileOnly(libs.findLibrary("annotations").get())

    // Tests
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
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

