package net.buildtheearth.terracore.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.*;
import static java.nio.charset.StandardCharsets.*;
import static java.util.concurrent.TimeUnit.*;
import static net.buildtheearth.terracore.http.HttpStatusCodes.*;
import static org.apache.logging.log4j.LogManager.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.*;

@Execution(CONCURRENT)
@Timeout(120)
public class TerracoreHttpClientTest {

    @Test
    void canSendSingleGetRequest() throws IOException {
        final var content = "Hello, World!";
        HttpHandler handler = (HttpExchange e) -> {
            e.sendResponseHeaders(HTTP_OK, 0);
            e.getResponseBody().write(content.getBytes(UTF_8));
            e.close();
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = noCacheClientBuilder().build()) {
                assertGetsContent(client, endpoint, content);
            }
        }
    }

    @Test
    void canSendConcurrentGetRequests() throws IOException {
        final var content = "Hello, World!";
        final var inFlightRequests = new AtomicInteger(0);
        final var maxInFlightRequests = new AtomicInteger(0);
        HttpHandler handler = (HttpExchange e) -> {
            var inFlight = inFlightRequests.incrementAndGet();
            maxInFlightRequests.updateAndGet(i -> Math.max(i, inFlight));
            uncheckedSleep(5, SECONDS);
            e.sendResponseHeaders(HTTP_OK, 0);
            e.getResponseBody().write(content.getBytes(UTF_8));
            e.close();
            inFlightRequests.getAndDecrement();
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = noCacheClientBuilder().build()) {
                var request1 = client.get(endpoint.url());
                var request2 = client.get(endpoint.url());
                var request3 = client.get(endpoint.url());

                assertSuccessfulFutureStringEquals(content, request1);
                assertSuccessfulFutureStringEquals(content, request2);
                assertSuccessfulFutureStringEquals(content, request3);

            }
        }
        assertEquals(3, maxInFlightRequests.get());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 16, 32, 64})
    void respectsHostConcurrencyThreshold(int threshold) throws IOException {
        final var requestProcessingMillis = 1_000;
        final var aimedRuntimeMillis = 10_000;
        final var requestCount = threshold * aimedRuntimeMillis / requestProcessingMillis;
        final var requests = new CompletableFuture<?>[requestCount];

        final var content = "Hello, World!";
        final var inFlightRequests = new AtomicInteger(0);
        final var maxInFlightRequests = new AtomicInteger(0);
        HttpHandler handler = (HttpExchange e) -> {
            var inFlight = inFlightRequests.incrementAndGet();
            maxInFlightRequests.updateAndGet(i -> Math.max(i, inFlight));
            uncheckedSleep(requestProcessingMillis, MILLISECONDS);
            e.sendResponseHeaders(HTTP_OK, 0);
            e.getResponseBody().write(content.getBytes(UTF_8));
            e.close();
            inFlightRequests.getAndDecrement();
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = noCacheClientBuilder().build()) {
                client.setHostConcurrencyThreshold(endpoint.url(), threshold);
                for (int i = 0; i < requests.length; i++) {
                    requests[i] = client.get(endpoint.url());
                }
                CompletableFuture.allOf(requests).join();
                assertEquals(threshold, maxInFlightRequests.get());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 16, 32, 64})
    void respectsDefaultConcurrencyThreshold(int threshold) throws IOException {
        final var requestProcessingMillis = 1_000;
        final var aimedRuntimeMillis = 10_000;
        final var requestCount = threshold * aimedRuntimeMillis / requestProcessingMillis;
        final var requests = new CompletableFuture<?>[requestCount];

        final var content = "Hello, World!";
        final var inFlightRequests = new AtomicInteger(0);
        final var maxInFlightRequests = new AtomicInteger(0);
        HttpHandler handler = (HttpExchange e) -> {
            var inFlight = inFlightRequests.incrementAndGet();
            maxInFlightRequests.updateAndGet(i -> Math.max(i, inFlight));
            uncheckedSleep(requestProcessingMillis, MILLISECONDS);
            e.sendResponseHeaders(HTTP_OK, 0);
            e.getResponseBody().write(content.getBytes(UTF_8));
            e.close();
            inFlightRequests.getAndDecrement();
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = noCacheClientBuilder().defaultConcurrencyThreshold(threshold).build()) {
                client.setHostConcurrencyThreshold(endpoint.url(), threshold);

                for (int i = 0; i < requests.length; i++) {
                    requests[i] = client.get(endpoint.url());
                }

                CompletableFuture.allOf(requests).join();
            }
        }
        assertEquals(threshold, maxInFlightRequests.get());
    }

    @Test
    void canSetUserAgent() throws IOException {
        final var userAgent = "Terracore unit test";
        HttpHandler handler = (HttpExchange e) -> {
            var userAgents = e.getRequestHeaders().get("User-Agent");
            assertEquals(List.of(userAgent), userAgents);
            e.sendResponseHeaders(HTTP_NO_CONTENT, -1);
            e.close();
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = noCacheClientBuilder().userAgent(userAgent).build()) {
                assertGetsContent(client, endpoint, "");
            }
        }
    }

    @Test
    void canCancelPendingRequests() throws IOException {
        final String content = "Should be canceled the second time";
        final AtomicInteger startedRequests = new AtomicInteger(0);
        HttpHandler handler = (HttpExchange e) -> {
            startedRequests.incrementAndGet();
            uncheckedSleep(10, SECONDS);
            e.sendResponseHeaders(HTTP_OK, 0);
            e.getResponseBody().write(content.getBytes());
            e.close();
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = noCacheClientBuilder().defaultConcurrencyThreshold(1).build()) {

                // Dispatch a first request to hit the concurrency threshold
                var request1 = client.get(endpoint.url());
                while (startedRequests.get() == 0) {
                    uncheckedSleep(100, MILLISECONDS);
                }

                // Then dispatch two requests, one of which is canceled after some time and one of which is canceled immediately
                var request2 = client.get(endpoint.url());  // Second request will be blocked by concurrency limits, since the threshold of 1 is reached
                var request3 = client.get(endpoint.url());  // Third request will be canceled before it has a chance to reach the concurrency semaphore
                request3.cancel(true);

                uncheckedSleep(5, SECONDS); // Give some time for the request to be blocked by concurrency limits
                request2.cancel(true);

                assertSuccessfulFutureStringEquals(content, request1);
                assertThrows(CancellationException.class, request2::join);
            }
        }
        assertEquals(1, startedRequests.get());
    }

    @Test
    void canCancelInFlightRequests() throws IOException {
        final int contentSize = 1_024;
        final var startedRequests = new AtomicInteger(0);
        final var completedRequests = new AtomicInteger(0);
        HttpHandler handler = (HttpExchange e) -> {
            startedRequests.incrementAndGet();
            try (var body = e.getResponseBody()) {
                e.sendResponseHeaders(HTTP_OK, contentSize);
                for (int i = 0; i < contentSize; i++) {
                    body.write(i & 0xFF);
                    uncheckedSleep(10, MILLISECONDS);
                }
            }
            completedRequests.incrementAndGet();
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = noCacheClientBuilder().build()) {
                var request = client.get(endpoint.url());

                // Wait for the request to be dispatched, then cancel it
                while (startedRequests.get() == 0) {
                    uncheckedSleep(100, MILLISECONDS);
                }
                request.cancel(true);

                assertThrows(CancellationException.class, request::join);
            }
        }
        assertEquals(1, startedRequests.get());
        assertEquals(0, completedRequests.get());
    }

    @Test
    void canCacheResponse() throws IOException {
        final String response = "Cached content";

        final AtomicInteger requestCounter = new AtomicInteger(0);

        HttpHandler handler = exchange -> {
            requestCounter.incrementAndGet();
            try (OutputStream os = exchange.getResponseBody()) {
                exchange.getResponseHeaders().set("Cache-Control", "max-age=100");
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(HTTP_OK, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = memoryCacheClientBuilder().build()) {
                assertGetsContent(client, endpoint, response);
                assertEquals(1, requestCounter.get());
                uncheckedSleep(500, MILLISECONDS);  // Wait for the cache to be written
                assertEquals(1, client.cacheStatistics().join().entries());
                assertGetsContent(client, endpoint, response); // Should hit the cache
            }
        }
        assertEquals(1, requestCounter.get());
    }

    @Test
    void canRefreshStaleResponseFromEtag() throws IOException {
        final String response = "Cached content";
        final String etag = "foobar";

        final AtomicInteger initialRequestCounter = new AtomicInteger(0);
        final AtomicInteger refreshRequestCounter = new AtomicInteger(0);

        HttpHandler handler = exchange -> {
            try (OutputStream os = exchange.getResponseBody()) {
                String expectedEtag = exchange.getRequestHeaders().getFirst("If-None-Match");
                if (expectedEtag != null && expectedEtag.equals(etag)) {
                    refreshRequestCounter.incrementAndGet();
                    exchange.sendResponseHeaders(HTTP_NOT_MODIFIED, -1);
                    return;
                }
                initialRequestCounter.incrementAndGet();
                exchange.getResponseHeaders().set("Cache-Control", "max-age=1");
                exchange.getResponseHeaders().set("Etag", etag);
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(HTTP_OK, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint(handler)) {
            try (var client = memoryCacheClientBuilder().build()) {
                assertGetsContent(client, endpoint, response);
                assertEquals(1, initialRequestCounter.get());
                assertEquals(0, refreshRequestCounter.get());
                uncheckedSleep(500, MILLISECONDS);  // Wait for the cache to be written
                assertEquals(1, client.cacheStatistics().join().entries());
                uncheckedSleep(2, SECONDS);  // Let the cache expire
                assertGetsContent(client, endpoint, response); // Should hit the cache but not be fresh
                assertEquals(1, client.cacheStatistics().join().entries());
            }
        }
        assertEquals(1, initialRequestCounter.get());
        assertEquals(1, refreshRequestCounter.get());
    }

    @Test
    void canRefreshStaleResponseFromLastModified() throws IOException {
        final String response = "Cached content";

        final AtomicInteger initialRequestCounter = new AtomicInteger(0);
        final AtomicInteger refreshRequestCounter = new AtomicInteger(0);

        HttpHandler handler = exchange -> {
            try (OutputStream os = exchange.getResponseBody()) {
                boolean ifModifiedSince = exchange.getRequestHeaders().containsKey("If-Modified-Since");
                if (ifModifiedSince) {
                    refreshRequestCounter.incrementAndGet();
                    exchange.sendResponseHeaders(HTTP_NOT_MODIFIED, -1);
                    return;
                }
                initialRequestCounter.incrementAndGet();
                exchange.getResponseHeaders().set("Cache-Control", "max-age=1");
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(HTTP_OK, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try(TestHttpEndpoint endpoint = new TestHttpEndpoint(handler)) {
            try (var client = memoryCacheClientBuilder().build()) {
                assertGetsContent(client, endpoint, response);
                assertEquals(1, initialRequestCounter.get());
                assertEquals(0, refreshRequestCounter.get());
                uncheckedSleep(500, MILLISECONDS);  // Wait for the cache to be written
                assertEquals(1, client.cacheStatistics().join().entries());
                uncheckedSleep(2, SECONDS);  // Let the cache expire
                assertGetsContent(client, endpoint, response); // Should hit the cache but not be fresh
                assertEquals(1, client.cacheStatistics().join().entries());
            }
        }
        assertEquals(1, initialRequestCounter.get());
        assertEquals(1, refreshRequestCounter.get());
    }

    @Test
    void notFoundReturnsNull() throws IOException {
        final String response = "Not Found";
        HttpHandler handler = exchange -> {
            try (OutputStream os = exchange.getResponseBody()) {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.getResponseHeaders().set("Etag", "my 404 content");
                exchange.sendResponseHeaders(HTTP_NOT_FOUND, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = memoryCacheClientBuilder().build()) {
                assertSuccessfulFutureStringEquals(null, client.get(endpoint.url()));
                uncheckedSleep(100, MILLISECONDS);
                assertSuccessfulFutureStringEquals(null, client.get(endpoint.url()));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {
            HTTP_CREATED, HTTP_ACCEPTED, HTTP_NON_AUTHORITATIVE_INFORMATION, HTTP_NO_CONTENT, HTTP_RESET_CONTENT, HTTP_PARTIAL_CONTENT, HTTP_MULTI_STATUS, HTTP_ALREADY_REPORTED, HTTP_IM_USED,
            HTTP_MULTIPLE_CHOICES, HTTP_NOT_MODIFIED, HTTP_USE_PROXY, HTTP_UNUSED,
            HTTP_BAD_REQUEST, HTTP_UNAUTHORIZED, HTTP_PAYMENT_REQUIRED, HTTP_FORBIDDEN, HTTP_METHOD_NOT_ALLOWED, HTTP_NOT_ACCEPTABLE, HTTP_PROXY_AUTHENTICATION_REQUIRED, HTTP_REQUEST_TIMEOUT, HTTP_CONFLICT, HTTP_GONE, HTTP_LENGTH_REQUIRED, HTTP_PRECONDITION_FAILED, HTTP_REQUEST_ENTITY_TOO_LARGE, HTTP_REQUEST_URI_TOO_LONG, HTTP_UNSUPPORTED_MEDIA_TYPE, HTTP_REQUESTED_RANGE_NOT_SATISFIABLE, HTTP_EXPECTATION_FAILED, HTTP_I_M_A_TEAPOT, HTTP_UNPROCESSABLE_ENTITY, HTTP_LOCKED, HTTP_FAILED_DEPENDENCY, HTTP_TOO_EARLY, HTTP_UPGRADE_REQUIRED, HTTP_PRECONDITION_REQUIRED, HTTP_TOO_MANY_REQUESTS, HTTP_REQUEST_HEADER_FIELDS_TOO_LARGE, HTTP_UNAVAILABLE_FOR_LEGAL_REASONS,
            HTTP_INTERNAL_SERVER_ERROR, HTTP_NOT_IMPLEMENTED, HTTP_BAD_GATEWAY, HTTP_SERVICE_UNAVAILABLE, HTTP_GATEWAY_TIMEOUT, HTTP_VERSION_NOT_SUPPORTED, HTTP_VARIANT_ALSO_NEGOTIATES, HTTP_INSUFFICIENT_STORAGE, HTTP_LOOP_DETECTED, HTTP_NOT_EXTENDED, HTTP_NETWORK_AUTHENTICATION_REQUIRED,
            208, 365, 473, 587, 999
    })
    void unsupportedHttpCodesThrowException(int statusCode) throws IOException {
        final String response = "Catch!";
        HttpHandler handler = exchange -> {
            try (OutputStream os = exchange.getResponseBody()) {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(statusCode, response.getBytes().length);
                os.write(response.getBytes());
            }
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = memoryCacheClientBuilder().build()) {
                try {
                    client.get(endpoint.url()).join();
                } catch (CompletionException e) {
                    assertInstanceOf(UnsupportedHttpStatus.class, e.getCause());
                }
                uncheckedSleep(100, MILLISECONDS);
                try {
                    client.get(endpoint.url()).join();
                } catch (CompletionException e) {
                    assertInstanceOf(UnsupportedHttpStatus.class, e.getCause());
                }
            }
        }
    }

    static void assertGetsContent(@NonNull HttpClient client, @NonNull TestHttpEndpoint endpoint, @Nullable String content) {
        var data = client.get(endpoint.url()).join();
        if (content == null) {
            assertNull(data);
        } else {
            assertNotNull(data);
            String responseString = new String(data);
            assertEquals(content, responseString);
        }
    }

    static void assertSuccessfulFutureStringEquals(@Nullable String exceptedResult, Future<byte @Nullable []> future) {
        try {
            var data = future.get();
            if (exceptedResult == null) {
                assertNull(data);
            } else {
                assertNotNull(data);
                var result = new String(data, UTF_8);
                assertEquals(exceptedResult, result);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    static void uncheckedSleep(long duration, TimeUnit unit) {
        try {
            sleep(unit.toMillis(duration));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static TerracoreHttpClient.TerracoreHttpClientBuilder memoryCacheClientBuilder() {
        return TerracoreHttpClient.builder()
                .logger(getLogger())
                .userAgent("TerracoreHttpClientTest")
                .cache(new MemoryCache());
    }

    static TerracoreHttpClient.TerracoreHttpClientBuilder noCacheClientBuilder() {
        return TerracoreHttpClient.builder()
                .logger(getLogger())
                .userAgent("TerracoreHttpClientTest")
                .cache(new EmptyCache());
    }

    static class TestHttpEndpoint implements Closeable {
        final HttpServer server;
        final ExecutorService executor;
        final String uri;

        TestHttpEndpoint(String uri, HttpHandler handler) throws IOException {
            this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            this.uri = uri;
            this.server.createContext(uri, handler);
            this.executor = Executors.newCachedThreadPool();
            this.server.setExecutor(this.executor);
            this.server.start();
        }
        TestHttpEndpoint(HttpHandler handler) throws IOException {
            this("/", handler);
        }

        public String url() {
            var address = this.server.getAddress();
            return "http://" + address.getHostName() + ":" + address.getPort() + this.uri;
        }

        @Override
        public void close() {
            this.server.stop(0);
            this.executor.shutdownNow();
        }

    }

}
