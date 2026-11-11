<div align="center">
    <h1>Terracore</h1>
    <img alt="GitHub License" src="https://img.shields.io/github/license/BTE-France/terracore?style=flat-square">
    <img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/BTE-France/terracore/gradle-check.yml?style=flat-square">
</div>



## What is it?

Terracore is a collection of Java libraries to assist in developing mods,
plugins or really anything that relates to Minecraft worlds generated from real life geographic data.

Its main use case is the BuildTheEarth project.

It is a hard fork of [Terra++](https://github.com/BuildTheEarth/terraplusplus)
and [Terra--](https://github.com/SmylerMC/terraminusminus).

## Libraries

- `terracore-geo` implements geographic and geometric processing primitives, including an implementation of the BTE projection.
- `terracore-http` implements an HTTP client specifically tailored to Terracore's use case (focused around efficient GET requests with caching)
- `terracore-minecraft` implements a data-model that can represent Minecraft concepts like block states
- `terracore-worldgen` implements the full dataset processing pipeline and world generation engine, and depends on the previous three
- `terracore-bukkit` implements helpers to convert between the `terracore-minecraft` data model and the Bukkit API


## Using Terracore

Get started with Terracore by reading [the documentation](https://terracore.btefrance.fr).
