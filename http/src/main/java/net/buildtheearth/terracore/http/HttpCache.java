package net.buildtheearth.terracore.http;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A cache for storing HTTP responses.
 * Implementations must be thread-safe.
 * <br>
 * Terracore provides several cache implementations:
 * <ul>
 *   <li>{@link DiskCache} is probably what you want: it stores entries as compressed files in a directory;</li>
 *   <li>{@link MemoryCache} is much simpler and stores everything in memory;</li>
 *   <li>{@link EmptyCache} is a no-op implementation that does not cache anything.</li>
 * </ul>
 *
 * @author Smyler
 */
public interface HttpCache {

    /**
     * Stores an HTTP response in the cache.
     *
     * @param uri           the URI of the response
     * @param lastModified  the last modified timestamp of the response, in seconds
     * @param maxAge        the maximum age of the response in seconds
     * @param etag          the entity tag of the response
     * @param immutable     whether the response is immutable
     * @param mustRevalidate whether the response must be revalidated
     * @param body          the body of the response
     *
     * @throws NullPointerException if uri or body is null
     */
    void put(@NonNull URI uri, long lastModified, long maxAge, @Nullable String etag, boolean immutable, boolean mustRevalidate, byte @NonNull [] body);

    /**
     * Looks up an HTTP response in the cache.
     *
     * @param uri the URI of the response
     * @return the cached response, or an empty optional if not found
     *
     * @throws NullPointerException if uri is null
     */
    @NonNull Optional<CacheEntry> lookup(@NonNull URI uri);

    /**
     * Retrieves statistics about the cache.
     *
     * @return the cache statistics
     * @throws IOException if an I/O error occurs
     */
    @NonNull CacheStatistics statistics() throws IOException;

    /**
     * Cleans up the cache by removing entries that match the given predicate.
     *
     * @param predicate the predicate to match against cache entries
     * @return the cache statistics after cleanup
     * @throws IOException if an I/O error occurs
     */
    @NonNull CacheStatistics cleanup(@NonNull Predicate<CacheEntry> predicate) throws IOException;

}
