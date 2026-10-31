plugins {
    id("net.buildtheearth.terracore.gradle.module")
}

dependencies {
    implementation("net.daporkchop.lib:binary:0.5.7-SNAPSHOT")  {
        exclude(group = "io.netty")
    }
    implementation("com.google.guava:guava:33.4.8-jre")
}

publishing {
    publications {
        create<MavenPublication>("terracore-minecraft") {
            groupId = project.group.toString()
            artifactId = "terracore-minecraft"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

