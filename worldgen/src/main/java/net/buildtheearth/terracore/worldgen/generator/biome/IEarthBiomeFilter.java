package net.buildtheearth.terracore.worldgen.generator.biome;

import net.buildtheearth.terracore.minecraft.Biome;
import net.buildtheearth.terracore.worldgen.generator.ChunkBiomesBuilder;
import net.buildtheearth.terracore.worldgen.generator.IEarthAsyncPipelineStep;
import net.buildtheearth.terracore.worldgen.util.ImmutableCompactArray;

/**
 * @author DaPorkchop_
 */
public interface IEarthBiomeFilter<D> extends IEarthAsyncPipelineStep<D, ImmutableCompactArray<Biome>, ChunkBiomesBuilder> {
}
