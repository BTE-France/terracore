package net.buildtheearth.terracore.worldgen.dataset.osm;

import java.util.Collection;
import java.util.Map;

import lombok.NonNull;
import net.buildtheearth.terracore.worldgen.dataset.geojson.Geometry;
import net.buildtheearth.terracore.worldgen.dataset.vector.geometry.VectorGeometry;

/**
 * Consumes a GeoJSON geometry object and emits some number of generateable elements.
 *
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface OSMMapper<G extends Geometry> {
    Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry);
}
