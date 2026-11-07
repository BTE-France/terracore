package net.buildtheearth.terracore.worldgen.dataset.geojson.object;

import lombok.Data;
import lombok.NonNull;
import net.buildtheearth.terracore.worldgen.dataset.geojson.GeoJsonObject;

/**
 * Non-standard GeoJSON object: represents a reference to another URL containing a GeoJSON object that this object should be substituted with.
 *
 * @author DaPorkchop_
 */
@Data
public final class Reference implements GeoJsonObject {
    @NonNull
    protected final String location;
}
