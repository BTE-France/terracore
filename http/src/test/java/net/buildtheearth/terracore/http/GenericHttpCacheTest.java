package net.buildtheearth.terracore.http;

import java.io.IOException;
import java.net.URI;
import java.util.Random;

import static java.lang.System.*;
import static org.junit.jupiter.api.Assertions.*;

public abstract class GenericHttpCacheTest {

    public static final long FAKE_NOW = currentTimeMillis();

    void canCacheEntry(HttpCache cache) throws IOException {
        cache.put(URI.create("https://localhost/foobar"), FAKE_NOW, 1_000L, "myEtag", false, false, randomData(100L, 53291L));
        cache.put(URI.create("http://example.com:8080/fizzbuzz"), FAKE_NOW + 100L, 6_565L, null, true, false, randomData(1_000L, 63491L));

        var entry = cache.lookup(URI.create("https://localhost/foobar")).orElseThrow();
        assertCacheEntryMetadata(FAKE_NOW, 1_000L, "myEtag", false, false, entry);
        assertCacheEntryBody(randomData(100L, 53291L), entry);

        entry = cache.lookup(URI.create("http://example.com:8080/fizzbuzz")).orElseThrow();
        assertCacheEntryMetadata(FAKE_NOW + 100L, 6_565L, null, true, false, entry);
        assertCacheEntryBody(randomData(1_000L, 63491L), entry);

        cache.put(URI.create("https://localhost/foobar"), FAKE_NOW + 7_000L, 0L, "myOtherEtag", true, true, randomData(45L, 64890L));
        entry = cache.lookup(URI.create("https://localhost/foobar")).orElseThrow();
        assertCacheEntryMetadata(FAKE_NOW + 7_000L, 0L, "myOtherEtag", true, true, entry);
        assertCacheEntryBody(randomData(45L, 64890L), entry);
    }

    void lookupReturnsEmptyForUnknownEntries(HttpCache cache) {
        assertTrue(cache.lookup(URI.create("https://localhost/unknown")).isEmpty());
        assertTrue(cache.lookup(URI.create("http://example.com:42")).isEmpty());
    }

    void bodyCanBeReadMultipleTimes(HttpCache cache) throws IOException {
        URI uri = URI.create("https://localhost/reusable");
        byte[] data = randomData(50, 987_654L);
        cache.put(uri, FAKE_NOW / 1_000, 100L, "reusableEtag", false, false, data);

        var entry = cache.lookup(uri).orElseThrow();
        assertCacheEntryBody(data, entry);
        assertCacheEntryBody(data, entry);
    }

    void canCacheEmptyBody(HttpCache cache) throws IOException {
        URI uri = URI.create("https://localhost/empty");
        cache.put(uri, FAKE_NOW / 1_000, 100L, null, true, true, new byte[0]);

        var entry = cache.lookup(uri).orElseThrow();
        assertCacheEntryMetadata(FAKE_NOW / 1_000, 100L, null, true, true, entry);
        assertCacheEntryBody(new byte[0], entry);
    }

    void statisticsAreEmptyOnNewCache(HttpCache cache, CacheStatistics.CacheType expectedType) throws IOException {
        var stats = cache.statistics();
        assertEquals(0, stats.entries());
        assertEquals(0, stats.size());
        assertEquals(expectedType, stats.type());
    }

    void statisticsReportPutEntries(HttpCache cache, CacheStatistics.CacheType expectedType) throws IOException {
        this.putSampleEntries(cache);

        var stats = cache.statistics();
        assertEquals(2, stats.entries());
        assertEquals(expectedType, stats.type());
        assertTrue(stats.size() > 0, "Cache should take up some space after two puts");
    }

    void cleanupRemovesNothingWhenPredicateDoesNotMatch(HttpCache cache) throws IOException {
        this.putSampleEntries(cache);

        var removedStats = cache.cleanup(entry -> false);
        assertEquals(0, removedStats.entries());
        assertEquals(0, removedStats.size());

        assertTrue(cache.lookup(URI.create("https://localhost/foo")).isPresent());
        assertTrue(cache.lookup(URI.create("https://localhost/bar")).isPresent());
    }

    void cleanupRemovesMatchingEntries(HttpCache cache) throws IOException {
        this.putSampleEntries(cache);
        long sizeBeforeCleanup = cache.statistics().size();

        var removedStats = cache.cleanup(entry -> entry.uri().equals(URI.create("https://localhost/foo")));
        assertEquals(1, removedStats.entries());
        assertTrue(removedStats.size() > 0);
        assertTrue(cache.lookup(URI.create("https://localhost/foo")).isEmpty(), "Removed entry should no longer be looked up");

        var remainingEntry = cache.lookup(URI.create("https://localhost/bar")).orElseThrow();
        assertCacheEntryBody(randomData(1_000L, 63491L), remainingEntry);

        var stats = cache.statistics();
        assertEquals(1, stats.entries());
        assertTrue(stats.size() > 0);
        assertTrue(stats.size() < sizeBeforeCleanup, "Cache should take up less space after cleanup");
    }

    void cleanupCanEmptyTheCache(HttpCache cache) throws IOException {
        this.putSampleEntries(cache);

        var removedStats = cache.cleanup(entry -> true);
        assertEquals(2, removedStats.entries());
        assertTrue(removedStats.size() > 0);

        assertEquals(0, cache.statistics().entries());
        assertTrue(cache.lookup(URI.create("https://localhost/foo")).isEmpty());
        assertTrue(cache.lookup(URI.create("https://localhost/bar")).isEmpty());
    }

    private void putSampleEntries(HttpCache cache) {
        cache.put(URI.create("https://localhost/foo"), FAKE_NOW / 1_000, 1_000L, "fooEtag", false, false, randomData(100L, 53291L));
        cache.put(URI.create("https://localhost/bar"), FAKE_NOW / 1_000, 1_000L, null, true, true, randomData(1_000L, 63491L));
    }

    void assertCacheEntryMetadata(long expectedLastModified, long expectedMaxAge, String expectedEtag, boolean expectedImmutable, boolean expectedMustRevalidate, CacheEntry entry) {
        assertEquals(expectedLastModified, entry.lastModified());
        assertEquals(expectedMaxAge, entry.maxAge());
        assertEquals(expectedEtag, entry.etag());
        assertEquals(expectedImmutable, entry.immutable());
        assertEquals(expectedMustRevalidate, entry.mustRevalidate());
    }

    void assertCacheEntryBody(byte[] expectedBody, CacheEntry entry) throws IOException {
        var bodyStream = entry.body().get();
        var body = bodyStream.readAllBytes();
        assertArrayEquals(expectedBody, body);
    }

    protected byte[] randomData(long size, long seed) {
        Random rand = new Random(seed);
        byte[] data = new byte[(int) size];
        rand.nextBytes(data);
        return data;
    }

}
