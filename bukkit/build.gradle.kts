plugins {
    id("net.buildtheearth.terracore.gradle.module")
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    api(project(":minecraft"))
    compileOnly(libs.spigot.api)

    testImplementation(project(":minecraft"))
    testImplementation(libs.spigot.api)
}

publishing {
    publications {
        create<MavenPublication>("terracore-bukkit") {
            groupId = project.group.toString()
            artifactId = "terracore-bukkit"
            version = project.version.toString()
            from(components["java"])
        }
    }
}