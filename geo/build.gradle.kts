plugins {
    id("net.buildtheearth.terracore.gradle.module")
}

dependencies {
    implementation(libs.jackson.databind)
    implementation(libs.daporkchop.binary) {
        exclude(group = "io.netty")
    }
    implementation(libs.guava)
    implementation(libs.netty.all)
}

publishing {
    publications {
        create<MavenPublication>("terracore-geo") {
            groupId = project.group.toString()
            artifactId = "terracore-geo"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

