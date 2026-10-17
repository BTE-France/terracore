package net.buildtheearth.terracore.http;

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
     * @return a future containing the response body as a byte array
     */
    CompletableFuture<byte[]> get(String url);

    /**
     * Sets the maximum number of concurrent requests allowed to a specific host.
     *
     * @param host                  the host for which to set the maximum number of concurrent requests
     * @param maxConcurrentRequests the maximum number of concurrent requests allowed for the specified host
     */
    void setMaxConcurrentRequests(String host, int maxConcurrentRequests);


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
