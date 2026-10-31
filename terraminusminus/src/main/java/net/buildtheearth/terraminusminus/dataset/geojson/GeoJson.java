package net.buildtheearth.terraminusminus.dataset.geojson;

import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terracore.minecraft.BlockState;
import net.buildtheearth.terraminusminus.TerraConstants;
import net.buildtheearth.terraminusminus.dataset.osm.BlockStateParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class GeoJson {

    static final Logger LOGGER = LogManager.getLogger(GeoJson.class);
    static final Gson GSON = new GsonBuilder().create();


    /**
     * Parses a single GeoJSON object from the given {@link Reader}.
     *
     * @param in the {@link Reader} to read from
     * @return the parsed GeoJSON object
     */
    public static GeoJsonObject parse(@NonNull Reader in) {
        return GSON.fromJson(in, GeoJsonObject.class);
    }

    /**
     * Parses a single GeoJSON object from the given {@link String}.
     *
     * @param json the {@link String} containing the JSON text
     * @return the parsed GeoJSON object
     */
    public static GeoJsonObject parse(@NonNull String json) {
        return GSON.fromJson(json, GeoJsonObject.class);
    }
}
