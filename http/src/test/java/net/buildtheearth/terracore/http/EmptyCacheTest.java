package net.buildtheearth.terracore.http;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static java.lang.System.*;
import static net.buildtheearth.terracore.http.CacheStatistics.CacheType.*;
import static org.junit.jupiter.api.Assertions.*;

public class EmptyCacheTest {

    @Test
    public void testEmptyCache() {
        EmptyCache cache = new EmptyCache();
        assertEmptyStatistics(cache.statistics());

        cache.put(URI.create("https://localhost/"), currentTimeMillis() / 1_000, 1_000, "foobar", false, false, new byte[0]);

        assertEmptyStatistics(cache.statistics());
        assertEmptyStatistics(cache.cleanup(ignored -> {
            throw new AssertionError("Cleanup predicated should not be called");
        }));
    }

    void assertEmptyStatistics(CacheStatistics stats) {
        assertEquals(0, stats.entries());
        assertEquals(0, stats.size());
        assertEquals(MEMORY, stats.type());
    }

}
