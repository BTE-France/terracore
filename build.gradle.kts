subprojects {
    apply(plugin = "java-library")
    apply(plugin = "eclipse")
    apply(plugin = "maven-publish")

    version = "git describe --dirty" //FIXME
    group = "net.buildtheearth.terracore"

    /*
    */

}


/*
publishing {
    publications {
        terraminusminus(MavenPublication) {
            groupId = project.group
            artifactId = "terraminusminus-core"
            version = project.version
            from(components["java"])
        }
    }
}

tasks.register("delombok") {
    description = "Delomboks the source code"
    group = 'publishing'
    var buildDir = layout.buildDirectory.get()
    ext.outputDir = file("$buildDir/delombok")
    outputs.dir(outputDir)
    sourceSets.main.java.srcDirs.each {
        inputs.dir(it)
        ant.taskdef(classname = "lombok.delombok.ant.Tasks$Delombok", classpath = configurations.compileClasspath.asPath, name = 'delombok')
        ant.mkdir(dir = outputDir)
        ant.delombok(verbose= "true", encoding = "UTF-8", to = outputDir, from = it)
    }
}

javadoc {
    dependsOn delombok
    source = delombok.outputDir
    failOnError = false
}
*/
