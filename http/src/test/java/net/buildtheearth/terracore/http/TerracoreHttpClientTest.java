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
            var client = TerracoreHttpClient.builder()
                    .logger(getLogger())
                    .userAgent("terracore unit test")
                    .cache(new MemoryCache())
                    .build();
            assertGetsContent(client, endpoint, content);
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
            var client = TerracoreHttpClient.builder()
                    .logger(getLogger())
                    .userAgent("terracore unit test")
                    .cache(new EmptyCache())
                    .build();
            var request1 = client.get(endpoint.url());
            var request2 = client.get(endpoint.url());
            var request3 = client.get(endpoint.url());

            assertSuccessfulFutureStringEquals(content, request1);
            assertSuccessfulFutureStringEquals(content, request2);
            assertSuccessfulFutureStringEquals(content, request3);

            assertEquals(3, maxInFlightRequests.get());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 16, 32, 64})
    void respectsConcurrencyThreshold(int threshold) throws IOException {
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
            var client = TerracoreHttpClient.builder()
                    .logger(getLogger())
                    .userAgent("terracore unit test")
                    .cache(new EmptyCache())
                    .build();
            client.setMaxConcurrentRequests(endpoint.url(), threshold);

            for (int i = 0; i < requests.length; i++) {
                requests[i] = client.get(endpoint.url());
            }

            CompletableFuture.allOf(requests).join();
            assertEquals(threshold, maxInFlightRequests.get());
        }
    }

    @Test
    void canSetUserAgent() throws IOException {
        final var userAgent = "Terracore unit test";
        var client = TerracoreHttpClient.builder()
                .logger(getLogger())
                .userAgent(userAgent)
                .cache(new MemoryCache())
                .build();
        HttpHandler handler = (HttpExchange e) -> {
            var userAgents = e.getRequestHeaders().get("User-Agent");
            assertEquals(List.of(userAgent), userAgents);
            e.sendResponseHeaders(HTTP_NO_CONTENT, -1);
            e.close();
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            assertGetsContent(client, endpoint, "");
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
            this.server.stop(10);
            this.executor.shutdownNow();
        }

    }

    private static int randPort() {
        Random random = new Random();
        return random.nextInt(65535 - 1024) + 1024;
    }

}
