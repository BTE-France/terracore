package net.buildtheearth.terracore.minecraft;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
final class BiomeImplementation implements Biome {

    @Getter
    private final @NonNull Identifier identifier;

    Object bukkitBiome = null;

    /*
     * A cache, to maintain object identity since there should be a final limited amount of biomes.
     * Capacity and maximum size has been chosen to fit all default Minecraft biomes by default (65 of them in 1.21.4),
     * and allow growth up to an amount that should fit all biomes even on modded while remaining a good safeguard.
     */
    private static final LoadingCache<@NotNull Identifier, @NotNull Biome> cache = CacheBuilder.newBuilder()
            .initialCapacity(100)
            .maximumSize(4096)
            .build(CacheLoader.from(BiomeImplementation::new));

    static Biome get(Identifier identifier) {
        return BiomeImplementation.cache.getUnchecked(identifier);
    }

    @Override
    public String toString() {
        return this.identifier.toString();
    }
}
