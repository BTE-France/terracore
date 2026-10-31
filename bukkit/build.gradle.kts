plugins {
    id("net.buildtheearth.terracore.gradle.module")
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    api(project(":minecraft"))
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")

    testImplementation(project(":minecraft"))
    testImplementation("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
}

publishing {
    publications {
        create<MavenPublication>("terraminusminus-bukkit") {
            groupId = project.group.toString()
            artifactId = "terraminusminus-bukkit"
            version = project.version.toString()
            from(components["java"])
        }
    }
}