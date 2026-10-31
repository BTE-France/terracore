package net.buildtheearth.terracore.worldgen.generator.data;

import net.buildtheearth.terracore.worldgen.generator.CachedChunkData;
import net.buildtheearth.terracore.worldgen.generator.IEarthAsyncPipelineStep;

/**
 * @author DaPorkchop_
 */
public interface IEarthDataBaker<D> extends IEarthAsyncPipelineStep<D, CachedChunkData, CachedChunkData.Builder> {
}
