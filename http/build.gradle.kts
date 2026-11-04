plugins {
    id("net.buildtheearth.terracore.gradle.module")
}

dependencies {
    implementation(libs.jackson.databind)
    implementation(libs.daporkchop.binary) {
        exclude(group = "io.netty")
    }
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.netty.all)
    implementation(libs.bundles.log4j)
}

tasks.processResources {
    // Inject the version into terracore.properties so the HTTP client can use it in its user agent
    inputs.property("version", project.version)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    filesMatching("/net/buildtheearth/terracore/http/terracore.properties") {
        expand(mapOf("version" to project.version))
    }
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

