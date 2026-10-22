plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("io.freefair.git-version:io.freefair.git-version.gradle.plugin:9.8.0")
    implementation("io.freefair.lombok:io.freefair.lombok.gradle.plugin:9.8.0")
}