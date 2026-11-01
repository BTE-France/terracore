package net.buildtheearth.terracore.worldgen.dataset.vector.draw;

import java.io.IOException;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terracore.minecraft.BlockState;
import net.buildtheearth.terracore.worldgen.dataset.osm.JsonParser;
import net.buildtheearth.terracore.worldgen.dataset.osm.OSMConfig;
import net.buildtheearth.terracore.worldgen.generator.CachedChunkData;

/**
 * {@link DrawFunction} which sets the surface block to a fixed block state.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Block.Parser.class)
@RequiredArgsConstructor
public final class Block implements DrawFunction {
    @NonNull
    protected final BlockState state;

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        data.surfaceBlocks()[x * 16 + z] = this.state;
    }

    static class Parser extends JsonParser<Block> {
        @Override
        public Block read(JsonReader in) throws IOException {
            return new Block(OSMConfig.GSON.fromJson(in, BlockState.class));
        }
    }
}
