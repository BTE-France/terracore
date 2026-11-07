plugins {
    id("net.buildtheearth.terracore.gradle.module")
}

dependencies {
    api(project(":geo"))
    api(project(":http"))
    api(project(":minecraft"))

    implementation(libs.commons.imaging)
    implementation(libs.jackson.databind)
    implementation(libs.daporkchop.binary) {
        exclude(group = "io.netty")
    }
    implementation(libs.fastutil)
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.netty.all)
    implementation(libs.bundles.log4j)
}

publishing {
    publications {
        create<MavenPublication>("terracore-worldgen") {
            groupId = project.group.toString()
            artifactId = "terracore-worldgen"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

