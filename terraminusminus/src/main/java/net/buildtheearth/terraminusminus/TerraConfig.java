package net.buildtheearth.terraminusminus;

import net.buildtheearth.terracore.minecraft.Biome;

public class TerraConfig {

	public static boolean reducedConsoleMessages = false;

	public static OSMOpts openstreetmap = new OSMOpts();

    public static BiomeOpts biomes = new BiomeOpts();

	public static class OSMOpts {
		public String[] servers = {
				"https://cloud.daporkchop.net/gis/osm/0/"
		};
	}

    public static class BiomeOpts {
        public Biome defaultBiome = Biome.parse("ocean");
        public Biome waterInlandBiome = Biome.parse("river");
        public Biome waterOceanBiome = Biome.parse("deep_ocean");
        public Biome nullIslandInnerBiome = Biome.parse("forest");
        public Biome nullIslandOuterBiome = Biome.parse("plains");
    }

}
