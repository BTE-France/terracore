<div align="center">
    <h1>Terracore</h1>
    <img alt="GitHub License" src="https://img.shields.io/github/license/BTE-France/terracore?style=flat-square">
    <img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/BTE-France/terracore/gradle-check.yml?style=flat-square">
</div>



## What is it?

Terracore is a collection of Java libraries to assist in developing mods or plugins that generate Minecraft worlds
based on real geographic data, around with the surrounding ecosystem.

Its intended use case if the BuildTheEarth project.

It is a hard fork of [Terra++](https://github.com/BuildTheEarth/terraplusplus)
and [Terra--](https://github.com/SmylerMC/terraminusminus).

## Libraries

- `terracore-geo` implements geographic and geometric processing primitives, including an implementation of the BTE projection.
- `terracore-http` implements an HTTP client specifically tailored to Terracore's use case (focused around efficient GET requests with caching)
- `terracore-minecraft` implements a data-model that can represent Minecraft concepts like block states
- `terracore-worldgen` implements the full dataset processing pipeline and world generation engine, and depends on the previous three
- `terracore-bukkit` implements helpers to convert between the `terracore-minecraft` data model and the Bukkit API

All libraries live under the `net.buildtheearth.terracore` maven group.

## Using Terracore

Simply add [Smyler's repository](https://maven.smyler.net/releases) to your build system and depend your dependencies on the Terracore libraries you need.

For example, with Gradle for a Bukkit plugin that uses Terracore to implement world generation, you would want:

```groovy
// Smyler's repository has the Terracore builds
repositories {
    maven {
        name = "Smyler Releases"
        url = "https://maven.smyler.net/releases/"
    }

    // Classic JCenter repository that has most of what we need
    jcenter()
}

dependencies {

    // Declare your dependencies on the Terracore libraries you need.
    // master-SNAPSHOT indicates to use the last commit built from master,
    // you can replace this with  a reference to another branch, or to a tag.
    compile 'net.buildtheart.terracore:terracore-worldgen:master-SNAPSHOT'
    compile 'net.buildtheart.terracore:terracore-bukkit:master-SNAPSHOT'

    // Your other dependencies would go down there...
}
```

### Versioning

Terracore uses [semantic versioning](https://semver.org/) for releases.
Development branches get -SNAPHOT version in [Smyler's snapshot maven repository](https://maven.smyler.net/#/snapshots/net/buildtheearth/terracore).

