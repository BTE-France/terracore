package net.buildtheearth.terracore.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static net.buildtheearth.terracore.http.CacheStatistics.CacheType.*;
import static org.junit.jupiter.api.Assertions.*;

public class MemoryCacheTest extends GenericHttpCacheTest {

    @Test
    void canCacheEntry() throws IOException {
        MemoryCache cache = new MemoryCache();
        this.canCacheEntry(cache);
    }

    @Test
    void lookupReturnsEmptyForUnknownEntries() {
        this.lookupReturnsEmptyForUnknownEntries(new MemoryCache());
    }

    @Test
    void bodyCanBeReadMultipleTimes() throws IOException {
        this.bodyCanBeReadMultipleTimes(new MemoryCache());
    }

    @Test
    void canCacheEmptyBody() throws IOException {
        this.canCacheEmptyBody(new MemoryCache());
    }

    @Test
    void statisticsAreEmptyOnNewCache() throws IOException {
        this.statisticsAreEmptyOnNewCache(new MemoryCache(), MEMORY);
    }

    @Test
    void statisticsReportPutEntries() throws IOException {
        this.statisticsReportPutEntries(new MemoryCache(), MEMORY);
    }

    @Test
    void cleanupRemovesNothingWhenPredicateDoesNotMatch() throws IOException {
        this.cleanupRemovesNothingWhenPredicateDoesNotMatch(new MemoryCache());
    }

    @Test
    void cleanupRemovesMatchingEntries() throws IOException {
        this.cleanupRemovesMatchingEntries(new MemoryCache());
    }

    @Test
    void cleanupCanEmptyTheCache() throws IOException {
        this.cleanupCanEmptyTheCache(new MemoryCache());
    }

    @Test
    void statisticsSizeIsTheSumOfBodyLengths() throws IOException {
        MemoryCache cache = new MemoryCache();
        cache.put(URI.create("https://localhost/foo"), 0L, 0L, null, false, false, new byte[100]);
        cache.put(URI.create("https://localhost/bar"), 0L, 0L, null, false, false, new byte[1_000]);

        var stats = cache.statistics();
        assertEquals(MEMORY, stats.type());
        assertEquals(1_100, stats.size());
    }

}
