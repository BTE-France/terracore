package net.buildtheearth.terracore.worldgen.generator;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terracore.minecraft.Biome;
import net.buildtheearth.terracore.worldgen.dataset.builtin.Climate;
import net.buildtheearth.terracore.worldgen.dataset.builtin.Soil;
import net.buildtheearth.terracore.worldgen.dataset.geojson.dataset.ParsingGeoJsonDataset;
import net.buildtheearth.terracore.worldgen.dataset.geojson.dataset.ReferenceResolvingGeoJsonDataset;
import net.buildtheearth.terracore.worldgen.dataset.geojson.dataset.TiledGeoJsonDataset;
import net.buildtheearth.terracore.worldgen.dataset.osm.OSMConfig;
import net.buildtheearth.terracore.worldgen.dataset.scalar.MultiScalarDataset;
import net.buildtheearth.terracore.worldgen.dataset.vector.GeoJsonToVectorDataset;
import net.buildtheearth.terracore.worldgen.dataset.vector.VectorTiledDataset;
import net.buildtheearth.terracore.worldgen.generator.biome.IEarthBiomeFilter;
import net.buildtheearth.terracore.worldgen.generator.biome.Terra121BiomeFilter;
import net.buildtheearth.terracore.worldgen.generator.data.HeightsBaker;
import net.buildtheearth.terracore.worldgen.generator.data.IEarthDataBaker;
import net.buildtheearth.terracore.worldgen.generator.data.InitialBiomesBaker;
import net.buildtheearth.terracore.worldgen.generator.data.NullIslandBaker;
import net.buildtheearth.terracore.worldgen.generator.data.OSMBaker;
import net.buildtheearth.terracore.worldgen.generator.data.TreeCoverBaker;

/**
 * Default processing pipelines for various earth generator processing steps.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class EarthGeneratorPipelines {
    public final String KEY_DATASET_HEIGHTS = "heights";
    public final String KEY_DATASET_OSM_RAW = "osm_raw";
    public final String KEY_DATASET_OSM_PARSED = "osm_parsed";
    public final String KEY_DATASET_TERRA121_PRECIPITATION = "terra121_precipitation";
    public final String KEY_DATASET_TERRA121_SOIL = "terra121_soil";
    public final String KEY_DATASET_TERRA121_TEMPERATURE = "terra121_temperature";
    public final String KEY_DATASET_TREE_COVER = "tree_cover";

    public final String KEY_DATA_TREE_COVER = "tree_cover";

    public static final OSMOpts OSM_OPTIONS = new OSMOpts();

    public static BiomeOpts BIOME_OPTIONS = new BiomeOpts();


    public Map<String, Object> datasets(@NonNull EarthGeneratorSettings settings) {
        Map<String, Object> m = new HashMap<>();

        m.put(KEY_DATASET_HEIGHTS, new MultiScalarDataset(KEY_DATASET_HEIGHTS, settings.useDefaultHeights()));

        ParsingGeoJsonDataset rawOsm = new ParsingGeoJsonDataset(OSM_OPTIONS.servers);
        m.put(KEY_DATASET_OSM_RAW, new TiledGeoJsonDataset(new ReferenceResolvingGeoJsonDataset(rawOsm)));
        m.put(KEY_DATASET_OSM_PARSED, new VectorTiledDataset(new GeoJsonToVectorDataset(rawOsm, OSMConfig.load(), settings.projection())));

        m.put(KEY_DATASET_TERRA121_PRECIPITATION, new Climate.Precipitation());
        m.put(KEY_DATASET_TERRA121_SOIL, new Soil());
        m.put(KEY_DATASET_TERRA121_TEMPERATURE, new Climate.Temperature());
        m.put(KEY_DATASET_TREE_COVER, new MultiScalarDataset(KEY_DATASET_TREE_COVER, settings.useDefaultTreeCover()));

        return m;
    }

    public IEarthBiomeFilter<?>[] biomeFilters(@NonNull EarthGeneratorSettings settings) {
        return new IEarthBiomeFilter<?>[] {new Terra121BiomeFilter() };
    }

    public IEarthDataBaker<?>[] dataBakers(@NonNull EarthGeneratorSettings settings) {
        return new IEarthDataBaker<?>[] {
            new InitialBiomesBaker(settings.biomeProvider()),
            new TreeCoverBaker(),
            new HeightsBaker(),
            new OSMBaker(),
            new NullIslandBaker()
        };
    }

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
