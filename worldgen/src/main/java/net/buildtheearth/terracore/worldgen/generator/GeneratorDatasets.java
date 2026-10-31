package net.buildtheearth.terracore.worldgen.generator;

import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terracore.geo.projection.GeographicProjection;
import net.buildtheearth.terracore.worldgen.util.CustomAttributeContainer;

import java.util.Map;

/**
 * Wrapper class which contains all of the datasets used for generation.
 *
 * @author DaPorkchop_
 */
@Getter
public class GeneratorDatasets extends CustomAttributeContainer {
    protected final GeographicProjection projection;

    public GeneratorDatasets(@NonNull EarthGeneratorSettings settings) {
        super(EarthGeneratorPipelines.datasets(settings));

        this.projection = settings.projection();
    }

    /**
     * Constructs GeneratorDatasets based on a custom map of datasets and provided GeographicProjection
     * @param datasets The custom datasets that the GeneratorDatasets hold
     * @param projection The geographic projection that the GeneratorDatasets returns data in
     */
    public GeneratorDatasets(@NonNull Map<String, Object> datasets, @NonNull GeographicProjection projection) {
        super(datasets);
        this.projection = projection;
    }
}
