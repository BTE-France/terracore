package net.buildtheearth.terracore.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
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
            uncheckedSleep(100, MILLISECONDS);
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

                assertEquals(3, maxInFlightRequests.get());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 16, 32, 64})
    void respectsHostConcurrencyThreshold(int threshold) throws IOException {
        final var content = "Hello, World!";
        final var inFlightRequests = new AtomicInteger(0);
        final var maxInFlightRequests = new AtomicInteger(0);
        final var requests = new CompletableFuture<?>[64];
        HttpHandler handler = (HttpExchange e) -> {
            var inFlight = inFlightRequests.incrementAndGet();
            maxInFlightRequests.updateAndGet(i -> Math.max(i, inFlight));
            uncheckedSleep(100, MILLISECONDS);
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
        final var content = "Hello, World!";
        final var inFlightRequests = new AtomicInteger(0);
        final var maxInFlightRequests = new AtomicInteger(0);
        final var requests = new CompletableFuture<?>[64];
        HttpHandler handler = (HttpExchange e) -> {
            var inFlight = inFlightRequests.incrementAndGet();
            maxInFlightRequests.updateAndGet(i -> Math.max(i, inFlight));
            uncheckedSleep(100, MILLISECONDS);
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
                assertEquals(threshold, maxInFlightRequests.get());
            }
        }
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
        final AtomicInteger requestCount = new AtomicInteger(0);
        HttpHandler handler = (HttpExchange e) -> {
            requestCount.incrementAndGet();
            uncheckedSleep(1, SECONDS);
            e.sendResponseHeaders(HTTP_OK, 0);
            e.getResponseBody().write(content.getBytes());
            e.close();
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            try (var client = noCacheClientBuilder().defaultConcurrencyThreshold(1).build()) {
                var request1 = client.get(endpoint.url());  // First request reaches the server
                var request2 = client.get(endpoint.url());  // Second request is queued since the concurrency threshold of 1 is reached

                request2.cancel(true);

                assertSuccessfulFutureStringEquals(content, request1);
                assertThrows(CancellationException.class, request2::join);

                assertEquals(1, requestCount.get());
                assertGetsContent(client, endpoint, content);
            }
        }
    }

    static void assertGetsContent(@NonNull HttpClient client, @NonNull TestHttpEndpoint endpoint, @NonNull String content) {
        var data = client.get(endpoint.url()).join();
        String responseString = new String(data);
        assertEquals(content, responseString);
    }

    static void assertSuccessfulFutureStringEquals(String exceptedResult, Future<byte[]> future) {
        try {
            var data = future.get();
            var result = new String(data, UTF_8);
            assertEquals(exceptedResult, result);
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

    static TerracoreHttpClient.TerracoreHttpClientBuilder noCacheClientBuilder() {
        return TerracoreHttpClient.builder()
                .logger(getLogger())
                .userAgent("TerracoreHttpClientTest")
                .cache(new EmptyCache());
    }

    static class TestHttpEndpoint implements Closeable {
        final InetSocketAddress address = new InetSocketAddress("127.0.0.1", randPort());
        final HttpServer server;
        final ExecutorService executor;
        final String uri;

        TestHttpEndpoint(String uri, HttpHandler handler) throws IOException {
            this.server = HttpServer.create(this.address, 0);
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
            return "http://" + this.address.getHostName() + ":" + this.address.getPort() + this.uri;
        }

        @Override
        public void close() {
            this.server.stop(0);
            this.executor.shutdownNow();
        }

    }

    private static int randPort() {
        Random random = new Random();
        return random.nextInt(65535 - 1024) + 1024;
    }

}
