package net.buildtheearth.terracore.worldgen.dataset.geojson.dataset;

import static net.daporkchop.lib.common.util.PorkUtil.uncheckedCast;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import net.buildtheearth.terracore.geo.TileLocation;
import net.buildtheearth.terracore.worldgen.dataset.IDataset;
import net.buildtheearth.terracore.worldgen.dataset.IElementDataset;
import net.buildtheearth.terracore.worldgen.dataset.TiledDataset;
import net.buildtheearth.terracore.worldgen.dataset.geojson.GeoJsonObject;
import net.buildtheearth.terracore.geo.projection.EquirectangularProjection;
import net.buildtheearth.terracore.geo.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terracore.geo.CornerBoundingBox2d;
import net.buildtheearth.terracore.geo.bvh.Bounds2d;

/**
 * @author DaPorkchop_
 */
public class TiledGeoJsonDataset extends TiledDataset<GeoJsonObject[]> implements IElementDataset<GeoJsonObject[]> {
    protected final IDataset<String, GeoJsonObject[]> delegate;

    public TiledGeoJsonDataset(@NonNull IDataset<String, GeoJsonObject[]> delegate) {
        super(new EquirectangularProjection(), 1.0d / 64.0d);

        this.delegate = delegate;
    }

    @Override
    public CompletableFuture<GeoJsonObject[]> load(@NonNull TileLocation key) throws Exception {
        return this.delegate.getAsync(String.format("tile/%d/%d.json", key.x(), key.y()));
    }

    @Override
    public CompletableFuture<GeoJsonObject[][]> getAsync(@NonNull CornerBoundingBox2d bounds) throws OutOfProjectionBoundsException {
        Bounds2d localBounds = bounds.fromGeo(this.projection).axisAlign();
        CompletableFuture<GeoJsonObject[]>[] futures = uncheckedCast(Arrays.stream(localBounds.toTiles(this.tileSize))
                .map(this::getAsync)
                .toArray(CompletableFuture[]::new));

        return CompletableFuture.allOf(futures).thenApply(unused ->
                Arrays.stream(futures)
                        .map(CompletableFuture::join)
                        .toArray(GeoJsonObject[][]::new));
    }
}
