plugins {
    id("net.buildtheearth.terracore.gradle.module")
}

dependencies {
    implementation(libs.daporkchop.binary) {
        exclude(group = "io.netty")
    }
    implementation(libs.guava)
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

