package net.buildtheearth.terraminusminus.dataset;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terracore.geo.TileLocation;
import net.buildtheearth.terracore.geo.projection.GeographicProjection;

@RequiredArgsConstructor
@Getter
public abstract class TiledDataset<V> extends Dataset<TileLocation, V> {
    @NonNull
    protected final GeographicProjection projection;
    protected final double tileSize;
}
