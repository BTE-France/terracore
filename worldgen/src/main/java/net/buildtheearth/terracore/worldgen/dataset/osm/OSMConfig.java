package net.buildtheearth.terracore.worldgen.dataset.osm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terracore.http.Disk;
import net.buildtheearth.terracore.minecraft.BlockState;
import net.buildtheearth.terracore.worldgen.dataset.geojson.Geometry;
import net.daporkchop.lib.binary.oio.reader.UTF8FileReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class OSMConfig {

    public final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BlockState.class, BlockStateParser.INSTANCE)
            .create();

    @SneakyThrows(IOException.class)
    public OSMMapper<Geometry> load() {
        Path path = Disk.configFile("osm.json5");
        try (JsonReader reader = new JsonReader(Files.exists(path)
                ? new UTF8FileReader(path.toString())
                : new InputStreamReader(OSMConfig.class.getResourceAsStream("osm.json5")))) {
            try {
                return GSON.fromJson(reader, Root.class);
            } catch (Exception e) {
                throw new JsonParseException(reader.toString(), e);
            }
        }
    }

}
