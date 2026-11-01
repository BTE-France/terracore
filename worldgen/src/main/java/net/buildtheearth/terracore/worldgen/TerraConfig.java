package net.buildtheearth.terracore.worldgen;

import net.buildtheearth.terracore.minecraft.Biome;

/**
 * @deprecated Global state shall be eliminated
 */
@Deprecated
public class TerraConfig {


    public static BiomeOpts biomes = new BiomeOpts();

    public static class BiomeOpts {
        public Biome defaultBiome = Biome.parse("ocean");
        public Biome waterInlandBiome = Biome.parse("river");
        public Biome waterOceanBiome = Biome.parse("deep_ocean");
        public Biome nullIslandInnerBiome = Biome.parse("forest");
        public Biome nullIslandOuterBiome = Biome.parse("plains");
    }

}
