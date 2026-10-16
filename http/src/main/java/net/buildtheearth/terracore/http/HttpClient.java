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

}
