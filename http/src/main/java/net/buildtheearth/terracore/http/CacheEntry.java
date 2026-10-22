package net.buildtheearth.terracore.http;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Supplier;

import static java.lang.Math.*;
import static java.lang.System.*;

/**
 * An entry in an HTTP cache.
 *
 * @param uri               the URI of the cached resource
 * @param lastModified      the last modified timestamp of the cached resource, in seconds since the Unix epoch
 * @param maxAge            the maximum age of the cached resource, in seconds
 * @param etag              the entity tag of the cached resource
 * @param immutable         whether the cached resource is immutable
 * @param mustRevalidate    whether the cached resource must be revalidated
 * @param body              a supplier for the input stream of the cached resource
 *
 * @author Smyler
 */
public record CacheEntry(@NonNull URI uri, long lastModified, long maxAge, @Nullable String etag, boolean immutable, boolean mustRevalidate, @NonNull Supplier<InputStream> body) {

    /**
     * Creates a new {@link CacheEntry} instance.
     */
    public CacheEntry { }

    /**
     * Returns true if the cached resource is fresh,
     * i.e., its age is less than its maximum age,
     * and it's fit to be used without revalidation.
     *
     * @return true if the cached resource is fresh, false otherwise
     */
    public boolean isFresh() {
        return currentTimeMillis() / 1_000 < this.lastModified + this.maxAge;
    }

    /**
     * Returns the last modified timestamp of the cached resource as a LocalDateTime object.
     *
     * @return the last modified timestamp of the cached resource as a LocalDateTime object
     */
    public LocalDateTime lastModifiedDateTime() {
        return LocalDateTime.ofEpochSecond(this.lastModified, 0, ZoneOffset.UTC);
    }

    /**
     * Returns the age of the cached resource in seconds.
     *
     * @return the age of the cached resource in seconds
     */
    public long age() {
        return round(currentTimeMillis() / 1000d) - this.lastModified;
    }

}
