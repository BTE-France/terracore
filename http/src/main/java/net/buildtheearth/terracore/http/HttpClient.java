package net.buildtheearth.terracore.http;

import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous HTTP client abstraction.
 * Implementations should focus on efficient GET requests with compliant caching.
 * Implementations should support cancellation of the {@link CompletableFuture}s.
 *
 * @author Smyler
 */
public interface HttpClient {

    CompletableFuture<byte[]> get(String url);

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
