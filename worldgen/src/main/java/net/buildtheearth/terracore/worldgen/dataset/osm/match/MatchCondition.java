package net.buildtheearth.terracore.worldgen.dataset.osm.match;

import com.google.gson.annotations.JsonAdapter;
import java.util.Map;
import lombok.NonNull;
import net.buildtheearth.terracore.worldgen.dataset.geojson.Geometry;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(MatchParser.class)
@FunctionalInterface
public interface MatchCondition {
    /**
     * Always returns {@code false}.
     */
    MatchCondition FALSE = (id, tags, originalGeometry, projectedGeometry) -> false;

    boolean test(
            String id,
            @NonNull Map<String, String> tags,
            @NonNull Geometry originalGeometry,
            @NonNull Geometry projectedGeometry);
}
