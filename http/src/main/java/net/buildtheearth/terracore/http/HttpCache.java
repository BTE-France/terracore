package net.buildtheearth.terracore.http;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.function.Predicate;

/**
 * A cache for storing HTTP responses.
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
     */
    void put(@NonNull URI uri, long lastModified, long maxAge, @Nullable String etag, boolean immutable, boolean mustRevalidate, byte @NonNull [] body);

    /**
     * Looks up an HTTP response in the cache.
     *
     * @param uri the URI of the response
     * @return the cached response, or null if not found
     */
    @Nullable CacheEntry lookup(URI uri);

    /**
     * Retrieves statistics about the cache.
     *
     * @return the cache statistics
     * @throws IOException if an I/O error occurs
     */
    CacheStatistics statistics() throws IOException;

    /**
     * Cleans up the cache by removing entries that match the given predicate.
     *
     * @param predicate the predicate to match against cache entries
     * @return the cache statistics after cleanup
     * @throws IOException if an I/O error occurs
     */
    CacheStatistics cleanup(@NonNull Predicate<CacheEntry> predicate) throws IOException;

}
