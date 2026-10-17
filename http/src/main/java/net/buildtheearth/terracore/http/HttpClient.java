package net.buildtheearth.terracore.http;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous HTTP client abstraction.
 * Implementations should focus on efficient GET requests with compliant caching.
 * Implementations should support cancellation of the {@link CompletableFuture}s.
 *
 * @author Smyler
 */
public interface HttpClient extends Closeable {

    /**
     * Performs an asynchronous GET request to the specified URL.
     *
     * @param url the URL to send the GET request to
     * @return a future containing the response body as a byte array, or null if the server returned a 404 Not Found response
     */
    @NonNull CompletableFuture<byte @Nullable []> get(@NonNull String url);

    /**
     * Sets the maximum number of concurrent requests allowed to a specific host.
     *
     * @param host                  the host for which to set the maximum number of concurrent requests,
     *                              this may be provided as a full URI, in which case the host will be extracted from it
     * @param maxConcurrentRequests the maximum number of concurrent requests allowed for the specified host
     */
    void setHostConcurrencyThreshold(@NonNull String host, int maxConcurrentRequests);


    /**
     * Computes statistics about the current cache usage.
     *
     * @return a future of the client's cache statistics
     */
    CompletableFuture<CacheStatistics> cacheStatistics();

    /**
     * Performs regular maintenance on cache, removing long stale entries etc.
     *
     * @return a future containing statistics on the entries that were removed from the cache
     */
    CompletableFuture<CacheStatistics> cacheCleanup();

    /**
     * Fully clears the cache.
     *
     * @return a future containing statistics on the entries that were removed from the cache
     */
    CompletableFuture<CacheStatistics> cacheClear();

}
