package net.buildtheearth.terracore.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static net.buildtheearth.terracore.http.CacheStatistics.CacheType.*;
import static org.apache.logging.log4j.LogManager.*;


public class DiskCacheTest extends GenericHttpCacheTest {

    @Test
    void canCacheEntry(@TempDir Path cacheDir) throws IOException {
        DiskCache cache = new DiskCache(cacheDir, getLogger());
        this.canCacheEntry(cache);
    }

    @Test
    void lookupReturnsEmptyForUnknownEntries(@TempDir Path cacheDir) {
        this.lookupReturnsEmptyForUnknownEntries(new DiskCache(cacheDir, getLogger()));
    }

    @Test
    void bodyCanBeReadMultipleTimes(@TempDir Path cacheDir) throws IOException {
        this.bodyCanBeReadMultipleTimes(new DiskCache(cacheDir, getLogger()));
    }

    @Test
    void canCacheEmptyBody(@TempDir Path cacheDir) throws IOException {
        this.canCacheEmptyBody(new DiskCache(cacheDir, getLogger()));
    }

    @Test
    void statisticsAreEmptyOnNewCache(@TempDir Path cacheDir) throws IOException {
        this.statisticsAreEmptyOnNewCache(new DiskCache(cacheDir, getLogger()), DISK);
    }

    @Test
    void statisticsReportPutEntries(@TempDir Path cacheDir) throws IOException {
        this.statisticsReportPutEntries(new DiskCache(cacheDir, getLogger()), DISK);
    }

    @Test
    void cleanupRemovesNothingWhenPredicateDoesNotMatch(@TempDir Path cacheDir) throws IOException {
        this.cleanupRemovesNothingWhenPredicateDoesNotMatch(new DiskCache(cacheDir, getLogger()));
    }

    @Test
    void cleanupRemovesMatchingEntries(@TempDir Path cacheDir) throws IOException {
        this.cleanupRemovesMatchingEntries(new DiskCache(cacheDir, getLogger()));
    }

    @Test
    void cleanupCanEmptyTheCache(@TempDir Path cacheDir) throws IOException {
        this.cleanupCanEmptyTheCache(new DiskCache(cacheDir, getLogger()));
    }

}
