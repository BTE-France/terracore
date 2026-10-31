plugins {
    id("net.buildtheearth.terracore.gradle.module")
}

dependencies {
    implementation(project(":geo"))
    implementation(project(":minecraft"))

    implementation("org.apache.commons:commons-imaging:1.0.0-alpha5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
    implementation("net.daporkchop.lib:binary:0.5.7-SNAPSHOT")  {
        exclude(group = "io.netty")
    }
    implementation("it.unimi.dsi:fastutil:8.5.16")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("io.netty:netty-all:4.2.3.Final")
    implementation("org.apache.logging.log4j:log4j-api:2.25.1")
    implementation("org.apache.logging.log4j:log4j-core:2.25.1")
}

tasks.processResources {
    inputs.property("version", project.version)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    // Replace stuff in terraminusminus.properties, nothing else
    filesMatching("**/terraminusminus.properties") {
        expand(mapOf("version" to project.version))
    }
}

publishing {
    publications {
        create<MavenPublication>("terraminusminus") {
            groupId = project.group.toString()
            artifactId = "terraminusminus-core"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

