package net.buildtheearth.terracore.worldgen.dataset.geojson.object;

import java.util.Map;
import lombok.Data;
import lombok.NonNull;
import net.buildtheearth.terracore.worldgen.dataset.geojson.GeoJsonObject;
import net.buildtheearth.terracore.worldgen.dataset.geojson.Geometry;

/**
 * @author DaPorkchop_
 */
@Data
public final class Feature implements GeoJsonObject {
    @NonNull
    protected final Geometry geometry;

    protected final Map<String, String> properties;
    protected final String id;
}
