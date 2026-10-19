package net.buildtheearth.terracore.http;

import lombok.Builder;
import lombok.NonNull;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.concurrent.Executors.*;
import static net.buildtheearth.terracore.http.CacheStatistics.CacheType.*;
import static net.buildtheearth.terracore.http.HttpStatusCodes.*;
import static net.buildtheearth.terracore.http.InternalUtils.*;


/**
 * Default {@link HttpClient} implementation provided by Terracore.
 * <br>
 * This implementation is based on the {@link java.net.http.HttpClient} client implementation,
 * and inherits is supports for HTTP 1.1 and HTTP 2.0.
 * It also implements compliant HTTP caching.
 * <br>
 * Create new {@link TerracoreHttpClient} instances with {@link TerracoreHttpClient#builder()}.
 *
 * @author Smyler
 */
public class TerracoreHttpClient implements HttpClient {

    private final ExecutorService workerExecutor = newThreadPerTaskExecutor(
            Thread.ofVirtual()
                    .name("Terracore HTTP worker ", 1)
                    .uncaughtExceptionHandler(this::unhandledException)
                    .factory()
    );

    private final String userAgent;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
            .withZone(ZoneId.of("GMT"));

    private final int defaultConcurrencyThreshold;
    private final ConcurrentHashMap<String, Semaphore> concurrentRequestsCounters = new ConcurrentHashMap<>();

    private final java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
            .executor(this.workerExecutor)
            .build();
    private final Logger logger;
    private final HttpCache cache;

    private static final float CACHE_HEURISTIC = 0.25f;

    /**
     * @param logger the logger to use.
     *               As a general rule of thumb :
     *              <ul>
     *                <li>at level <code>TRACE</code>, an arbitrary number of log events may be emitted per request;</li>
     *                <li>at level <code>DEBUG</code>, a single event shall be emitted per request, upon completion;</li>
     *                <li>at level <code>INFO</code>, no events shall be emitted for successful requests (HTTP status code 1XX and 2XX);</li>
     *                <li>at level <code>WARN</code>, no events shall be emitted for requests, only for client-side recoverable errors;</li>
     *                <li>at level <code>ERROR</code>, no events shall be emitted for requests, only for client-side irrecoverable errors.</li>
     *              </ul>
     *
     * @param userAgent the user agent to use when making requests.
     * @param cache the cache to use for caching responses, if unspecified, a warning will be logged and an {@link MemoryCache} instance will be created and used.
     * @param defaultConcurrencyThreshold the default concurrency threshold to use when making requests, if unspecified, defaulting to 6.
     */
    @Builder
    TerracoreHttpClient(@NonNull Logger logger, @NonNull String userAgent, @Nullable HttpCache cache, int defaultConcurrencyThreshold) {
        this.logger = logger;
        this.userAgent = userAgent;
        this.defaultConcurrencyThreshold = defaultConcurrencyThreshold;

        if (cache == null) {
            this.logger.warn("No cache implementation provided, defaulting to in-memory cache");
            this.cache = new MemoryCache();
        } else {
            this.cache = cache;
        }
    }

    @Override
    public @NonNull CompletableFuture<byte[]> get(@NonNull String url) {
        URI uri = URI.create(url);
        var future = new InterruptibleFuture<byte[]>();
        this.workerExecutor.execute(() -> {
            try (var ignored = future.enterInterruptibleScope()) {
                var data = this.doGet(future, uri);
                future.complete(data);
            } catch (CancellationException e) {
                if (!future.isCancelled()) {
                    this.logger.error("Got CancellationException for uncancelled request future, something is not right");
                }
                // The throwing code is responsible for logging the cancellation point
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    @Override
    public @NonNull CompletableFuture<byte @Nullable []> getFirst(@NonNull String... urls) {
        if (urls.length == 0) {
            throw new IllegalArgumentException("No URLs provided");
        }
        if (urls.length == 1) {
            return this.get(urls[0]);
        }

        var uris = Arrays.stream(urls).map(URI::create).toArray(URI[]::new);

        var future = new InterruptibleFuture<byte[]>();
        this.workerExecutor.execute(() -> {
            try (var ignored = future.enterInterruptibleScope()) {
                var data = this.doGetSequence(future, uris);
                future.complete(data);
            } catch (CancellationException e) {
                if (!future.isCancelled()) {
                    this.logger.error("Got CancellationException for sequence uncancelled future, something is not right");
                }
                // The throwing code is responsible for logging the cancellation point
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    /**
     * Synchronously performs a single GET request.
     * This is expected to be running in a virtual thread and might be interrupted if the request's future gets canceled, so blocking is fine.
     *
     * @param future the future that will complete with the result, so the method can check for cancellation (it MUST NOT complete the future itself)
     * @param uri    the URI of the request
     *
     * @return the data to complete the future with, or null if the response was a 404
     *
     * @throws CancellationException if the request was canceled
     */
    private byte @Nullable[] doGet(InterruptibleFuture<byte[]> future, @NonNull URI uri) throws CancellationException {
        var requestId = UUID.randomUUID();
        var hostname = uri.getHost();
        this.logger.trace("[{}] New request: {}", requestId, uri);

        if (future.isCancelled()) {
            this.logger.trace("[{}] Request canceled before permit acquisition", requestId);
            throw new CancellationException();
        }

        // Lookup cache, use it if fresh
        CacheEntry cache = this.cache.lookup(uri).orElse(null);
        if (cache != null && cache.isFresh()) {
            byte[] data =  this.readCache(cache);
            this.logger.debug("[   ] {} {} (was fresh)", data.length, uri);
            return data;
        }

        // Prepare everything we need for the request
        final var request = this.createRequest(uri, cache);
        final var permit = this.getHostConcurrencySemaphore(hostname);
        HttpResponse<byte[]> response;

        try {
            this.logger.trace("[{}] Waiting for permit: {}", requestId, hostname);
            permit.acquire();
            this.logger.trace("[{}] Acquired permit: {}", requestId, hostname);
        } catch (InterruptedException e) {
            if (future.isCancelled()) {
                this.logger.trace("[{}] Request canceled while acquiring permit: {}", requestId, hostname);
                throw new CancellationException();
            }
            this.logger.error("[{}] Interrupted while acquiring permit for {}", requestId, hostname);
            throw new RuntimeException(e);
        }

        try {
            response = this.doRequest(request, requestId, future);
        } finally {
            permit.release();
            this.logger.trace("[{}] Released permit for {}", requestId, hostname);
        }

        var content = this.getContent(response, cache);

        // And update the cache async
        CompletableFuture.runAsync(() -> this.cacheResponse(requestId, response, content), this.workerExecutor);

        return content;
    }

    private byte @Nullable[] doGetSequence(InterruptibleFuture<byte[]> future, @NonNull URI... uris) throws CancellationException {
        var sequenceId = UUID.randomUUID();
        this.logger.trace("[{}] Sequence started ({} URLs to try)", sequenceId, uris.length);
        var exceptions = new ArrayList<Throwable>(uris.length);
        var count = 0;
        for (var uri : uris) {
            try {
                this.logger.trace("[{}][{}/{}] Trying {}", sequenceId, ++count, uris.length, uri);
                var content = this.doGet(future, uri);
                if (content != null) {
                    this.logger.trace("[{}][{}/{}] Sequence succeeded with {}", sequenceId, count, uris.length, uri);
                    return content;
                }
                this.logger.trace("[{}][{}/{}] Sequence continuing", sequenceId, count, uris.length);
            } catch (CancellationException e) {
                this.logger.trace("[{}][{}/{}] Sequence canceled", sequenceId, count, uris.length);
                throw e;
            } catch (Throwable t) {
                // doGet(...) has already logged the error
                exceptions.add(t);
            }
        }
        if (!exceptions.isEmpty()) {
            var exception = exceptions.getFirst();
            if (exception instanceof RuntimeException re) {
                throw re;
            } else {
                throw new RuntimeException(exception);
            }
        }

        return null;  // All 404s
    }

    private @NonNull HttpResponse<byte[]> doRequest(@NonNull HttpRequest request, @NonNull UUID requestId, @NonNull InterruptibleFuture<byte[]> future) {
        this.logger.trace(
                "[{}] {} {} {}",
                requestId,
                request.method(),
                request.version().orElse(this.client.version()).toString(),
                request.uri()
        );

        var responseFuture = this.client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());

        try {
            var response = responseFuture.get();
            this.logger.trace(
                    "[{}] {} {} {} {} {}",
                    requestId,
                    response.statusCode(),
                    response.headers().firstValue("Content-Type").orElse("application/unknown"),
                    request.method(),
                    response.version(),
                    request.uri()
            );
            return response;
        } catch (InterruptedException e) {
            if (!future.isCancelled()) {
                this.logger.error("[{}] Unexpected interruption while sending request", requestId);
                throw new RuntimeException(e);
            }
            this.logger.trace("[{}] Request canceled in-flight", requestId);
            responseFuture.cancel(true);
            throw new CancellationException();
        } catch (CancellationException e) {
            this.logger.error("[{}] Unexpected cancellation of the response future", requestId);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            this.logger.error("[{}] Error while making request", requestId);
            this.logger.catching(e);
            throw new RuntimeException(e.getCause());
        }

    }

    private HttpRequest createRequest(@NonNull URI uri, @Nullable CacheEntry cache) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.GET()
                .uri(uri)
                .header("User-Agent", this.userAgent);
        if (cache != null) {
            if (cache.etag() != null) {
                builder.header("If-None-Match", cache.etag());
            } else {
                builder.header("If-Modified-Since", DATE_TIME_FORMATTER.format(cache.lastModifiedDateTime()));
            }
        }
        return builder.build();
    }

    private byte @Nullable [] getContent(HttpResponse<byte[]> response, CacheEntry cache) {
        byte[] content = switch (response.statusCode()) {
            case HTTP_NOT_FOUND -> null;
            case HTTP_NOT_MODIFIED -> {
                if (cache == null) {
                    throw new UnsupportedHttpStatus(response.statusCode());
                }
                yield this.readCache(cache);
            }
            case HTTP_NO_CONTENT -> new byte[0];
            case HTTP_OK -> response.body();
            default -> throw new UnsupportedHttpStatus(response.statusCode());
        };
        this.logger.debug(
                "[{}] {} {} (was {})",
                response.statusCode(), content != null ? content.length : -1, response.uri(),
                cache == null ? "not cached": cache.isFresh() ? "fresh" : "stale"
        );
        return content;
    }

    private void cacheResponse(UUID requestId, HttpResponse<byte[]> response, byte[] content) {
        int statusCode = response.statusCode();
        if (statusCode != HTTP_OK && statusCode != HTTP_NO_CONTENT && statusCode != HTTP_NOT_MODIFIED) {
            return;  // We don't support caching other statuses at this point
        }
        Optional<String> header = response.headers().firstValue("Cache-Control");
        if (header.isPresent()) {
            this.cacheResponseWithCacheControl(requestId, response, header.get(), content);
        } else {
            this.cacheResponseWithHeuristics(requestId, response, content);
        }
    }

    private void cacheResponseWithCacheControl(UUID requestId, HttpResponse<byte[]> response, String cacheControl, byte[] content) {
        CacheControlResponseDirectives directives = CacheControlResponseDirectives.from(cacheControl);

        if (directives.noStore() || directives.mustUnderstand()) {
            this.logger.trace("[{}] Cache no-store for {}", requestId, response.uri());
            return;
        }

        String etag = response.headers().firstValue("Etag")
                .orElse(null);
        Optional<Long> expires = response.headers().firstValue("Expires")
                .flatMap(this::parseHttpDate);
        Optional<Long> date = response.headers().firstValue("Date")
                .flatMap(this::parseHttpDate);

        long age = response.headers().firstValue("Age")
                .flatMap(InternalUtils::parseOptionalLong)
                .orElse(0L);
        long lastModified = response.headers()
                .firstValue("Last-Modified")
                .flatMap(this::parseHttpDate)
                .orElseGet(() -> date.orElse(round(currentTimeMillis() / 1_000d) - age));
        boolean immutable = directives.immutable();
        long maxAge;

        if (directives.noCache()) {
            maxAge = 0;
        } else {
            maxAge = directives.maxAge()
                    .or(optionalBiMapSupplier(expires, date, (e, d) -> e - d))
                    .orElse(0L);
        }

        URI uri = response.uri();
        this.logger.trace(
                "[{}] Cache control for {}, lastModified={} age={} max-age={} etag='{}' immutable={}",
                requestId, uri, lastModified, age, maxAge, etag, immutable
        );
        this.cache.put(response.uri(), lastModified, maxAge, etag, immutable, directives.mustRevalidate(), content);
    }

    private void cacheResponseWithHeuristics(UUID requestId, HttpResponse<byte[]> response, byte[] content) {
        String etagHeader = response.headers().firstValue("Etag")
                .orElse(null);
        long date = response.headers().firstValue("Date")
                .flatMap(this::parseHttpDate)
                .orElseGet(() -> round(currentTimeMillis() / 1_000d));
        long lastModified = response.headers().firstValue("Last-Modified")
                .flatMap(this::parseHttpDate)
                .orElse(date);
        long age = response.headers().firstValue("Age")
                .flatMap(InternalUtils::parseOptionalLong)
                .orElse(date - lastModified);
        long maxAge = max(0, round(age * CACHE_HEURISTIC));
        this.logger.trace(
                "[{}] Cache heuristic for {}: lastModified={} age={} maxAge={} etag={}",
                requestId, response.uri(), lastModified, age, maxAge, etagHeader
        );
        this.cache.put(response.uri(), lastModified, maxAge, etagHeader, false, true, content);
    }

    private Optional<Long> parseHttpDate(String date) {
        return Optional.ofNullable(
                switch(DATE_TIME_FORMATTER.parse(date)) {
                    case LocalDateTime e -> e.toEpochSecond(ZoneOffset.UTC);
                    case ZonedDateTime zt -> zt.toEpochSecond();
                    default -> null;
                }
        );
    }

    private byte[] readCache(final CacheEntry cache) {
        try (InputStream in = cache.body().get()) {
            return in.readAllBytes();
        } catch (IOException e) {
            this.logger.warn("Error while reading HTTP cache");
            this.logger.catching(e);
            return new byte[0];
        }
    }

    @Override
    public void setHostConcurrencyThreshold(@org.jspecify.annotations.NonNull String host, int maxConcurrentRequests) {
        // We need to replace the old semaphore, we can't just release to the desired number,
        // that could cause a thread condition if a request is pending
        host = URI.create(host).getHost();
        if (isNullOrEmpty(host)) {
            return;
        }
        this.logger.debug("Setting max concurrent request to host {} to {}", host, maxConcurrentRequests);
        Semaphore newSemaphore = new Semaphore(maxConcurrentRequests);
        this.concurrentRequestsCounters.put(host, newSemaphore);
    }

    @Override
    public CompletableFuture<CacheStatistics> cacheStatistics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.cache.statistics();
            } catch (IOException e) {
                this.logger.warn("Computing cache statistics failed");
                this.logger.catching(e);
                return new CacheStatistics(0, 0, ERROR);
            }
        }, this.workerExecutor);
    }

    @Override
    public CompletableFuture<CacheStatistics> cacheCleanup() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.cache.cleanup(e -> !e.isFresh() && e.age() > e.maxAge() * 10);
            } catch (IOException e) {
                this.logger.warn("Cache cleanup failed");
                this.logger.catching(e);
                return new CacheStatistics(0, 0, ERROR);
            }
        }, this.workerExecutor);
    }

    @Override
    public CompletableFuture<CacheStatistics> cacheClear() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.cache.cleanup(e -> true);
            } catch (IOException e) {
                this.logger.warn("Cache clear failed");
                this.logger.catching(e);
                return new CacheStatistics(0, 0, ERROR);
            }
        }, this.workerExecutor);
    }

    @Override
    public void close() {
        this.client.close();
        this.workerExecutor.close();
    }

    private void unhandledException(Thread thread, Throwable throwable) {
        this.logger.error("Unhandled exception in HTTP client in thread {}, we might be leaking futures", thread.getName());
        this.logger.catching(throwable);
    }

    private @NonNull Semaphore getHostConcurrencySemaphore(@NonNull String hostname) {
        return this.concurrentRequestsCounters.computeIfAbsent(hostname, k -> new Semaphore(this.defaultConcurrencyThreshold));
    }

    private static class CacheControlResponseDirectives {
        private final Map<String, String> content = new HashMap<>();

        public boolean mustRevalidate() {
            return this.content.containsKey("must-revalidate");
        }

        public boolean noCache() {
            // In reality, no-cache is more powerful than just a boolean switch
            return this.content.containsKey("no-cache");
        }

        public boolean noStore() {
            return this.content.containsKey("no-store");
        }

        public boolean immutable() {
            return this.content.containsKey("immutable");
        }

        public boolean mustUnderstand() {
            return this.content.containsKey("must-understand");
        }

        public Optional<Long> maxAge() {
            return Optional.ofNullable(this.content.get("max-age")).flatMap(InternalUtils::parseOptionalLong);
        }

        public static CacheControlResponseDirectives from(String headerValue) {
            CacheControlResponseDirectives directives = new CacheControlResponseDirectives();
            stream(headerValue.split(","))
                    .map(String::strip)
                    .map(s -> s.split("=", 2))
                    .forEach(kv -> directives.content.put(kv[0].toLowerCase(), kv.length > 1 ? kv[1] : null));
            return directives;
        }

    }

    /**
     * Builder for {@link TerracoreHttpClient}.
     */
    @SuppressWarnings({ "unused", "FieldMayBeFinal" })  // Lombok auto-completes the class
    public static class TerracoreHttpClientBuilder {
        private int defaultConcurrencyThreshold = 6;

        TerracoreHttpClientBuilder() { }
    }

}