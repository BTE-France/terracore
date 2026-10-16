plugins {
    id("net.buildtheearth.terracore.gradle.module")
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.26.1")
    testImplementation("org.apache.logging.log4j:log4j-core:2.26.1")
}

publishing {
    publications {
        create<MavenPublication>("terracore-http") {
            groupId = project.group.toString()
            artifactId = "terracore-http"
            version = project.version.toString()
            from(components["java"])
        }
    }
}