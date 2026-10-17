package net.buildtheearth.terracore.http;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Optional;
import java.util.function.Predicate;

import static net.buildtheearth.terracore.http.CacheStatistics.CacheType.*;

/**
 * An implementation of {@link HttpCache} that does nothing:
 * <ul>
 * <li>{@link EmptyCache#put(URI, long, long, String, boolean, boolean, byte[])} has no effect;</li>
 * <li>{@link EmptyCache#lookup(URI)} always returns an empty {@link Optional};</li>
 * <li>{@link EmptyCache#statistics()} always returns an empty {@link CacheStatistics};</li>
 * <li>{@link EmptyCache#cleanup(Predicate)} has no effect and returns an empty {@link CacheStatistics}.</li>
 * </ul>
 */
public class EmptyCache implements HttpCache {

    private static final CacheStatistics EMPTY_STATISTICS = new CacheStatistics(0, 0, MEMORY);

    @Override
    public void put(@NonNull URI uri, long lastModified, long maxAge, @Nullable String etag, boolean immutable, boolean mustRevalidate, byte @NonNull [] body) {
        // No-op
    }

    @Override
    public @NonNull Optional<CacheEntry> lookup(@NonNull URI uri) {
        return Optional.empty();
    }

    @Override
    public @NonNull CacheStatistics statistics() {
        return EMPTY_STATISTICS;
    }

    @Override
    public @NonNull CacheStatistics cleanup(@NonNull Predicate<CacheEntry> predicate) {
        return EMPTY_STATISTICS;
    }

}
