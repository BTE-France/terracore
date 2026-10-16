package net.buildtheearth.terracore.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
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

public class TerracoreHttpClientTest {

    @Test
    void canSendSingleGetRequest(@TempDir Path cacheDir) throws IOException {
        final var content = "Hello, World!";
        HttpHandler handler = (HttpExchange e) -> {
            e.sendResponseHeaders(HTTP_OK, 0);
            e.getResponseBody().write(content.getBytes(UTF_8));
            e.close();
        };
        try (var endpoint = new TestHttpEndpoint(handler)) {
            var client = new TerracoreHttpClient(getLogger(), cacheDir);
            assertGetsContent(client, endpoint, content);
        }
    }

    @Test
    void canSendConcurrentGetRequests(@TempDir Path cacheDir) throws IOException {
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
            var client = new TerracoreHttpClient(getLogger(), cacheDir);
            var request1 = client.get(endpoint.url() + "?cacheBurst=1");
            var request2 = client.get(endpoint.url() + "?cacheBurst=2");
            var request3 = client.get(endpoint.url() + "?cacheBurst=3");

            assertSuccessfulFutureStringEquals(content, request1);
            assertSuccessfulFutureStringEquals(content, request2);
            assertSuccessfulFutureStringEquals(content, request3);

            assertEquals(3, maxInFlightRequests.get());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 16, 32, 64})
    void respectsConcurrencyThreshold(int threshold, @TempDir Path cacheDir) throws IOException {
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
            var client = new TerracoreHttpClient(getLogger(), cacheDir);
            client.setMaxConcurrentRequests(endpoint.url(), threshold);

            for (int i = 0; i < requests.length; i++) {
                requests[i] = client.get(endpoint.url() + "?cacheBurst=" + i);
            }

            CompletableFuture.allOf(requests).join();
            assertEquals(threshold, maxInFlightRequests.get());
        }
    }


    static void assertGetsContent(@NonNull HttpClient client, @NonNull TestHttpEndpoint endpoint, @NonNull String content) {
        var data = client.get(endpoint.url()).join();
        String responseString = new String(data);
        assertEquals(content, responseString);
    }

    static <T> void assertSuccessfulFutureStringEquals(String exceptedResult, Future<byte[]> future) {
        try {
            var data = future.get();
            var result = new String(data, UTF_8);
            assertEquals(exceptedResult, result);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> void assertSuccessfulFutureEquals(T exceptedResult, Future<T> future) {
        try {
            T result = future.get();
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
