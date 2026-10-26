plugins {
    id("net.buildtheearth.terracore.gradle.module")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
    implementation("net.daporkchop.lib:binary:0.5.7-SNAPSHOT")  {
        exclude(group = "io.netty")
    }
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("io.netty:netty-all:4.2.3.Final")
    implementation("org.apache.logging.log4j:log4j-api:2.25.1")
    implementation("org.apache.logging.log4j:log4j-core:2.25.1")
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

