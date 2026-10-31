package net.buildtheearth.terracore.worldgen.dataset.geojson;

import com.google.gson.annotations.JsonAdapter;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(ObjectDeserializer.class)
public interface GeoJsonObject {
}
