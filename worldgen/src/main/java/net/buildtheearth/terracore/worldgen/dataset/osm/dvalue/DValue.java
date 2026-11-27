package net.buildtheearth.terracore.worldgen.dataset.osm.dvalue;

import com.google.gson.annotations.JsonAdapter;
import java.util.Map;
import lombok.NonNull;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(DValueParser.class)
@FunctionalInterface
public interface DValue {
    double apply(@NonNull Map<String, String> tags);
}
