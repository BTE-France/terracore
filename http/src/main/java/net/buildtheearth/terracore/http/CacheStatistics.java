package net.buildtheearth.terracore.http;

import lombok.NonNull;

/**
 * Cache usage statistics.
 *
 * @param entries   number of entries in the cache
 * @param size      estimated total storage space used by the cache, in bytes
 * @param type      the storage medium of the cache
 */
public record CacheStatistics(long entries, long size, @NonNull CacheType type) {

    /**
     * Creates a new {@link CacheStatistics} instance.
     */
    public CacheStatistics { }

    /**
     * An enum representing the storage medium of the cache.
     */
    public enum CacheType {

        /**
         * Cache is fully on disk.
         */
        DISK,

        /**
         * Cache is fully in memory.
         */
        MEMORY,

        /**
         * Cache uses a mix of disk and memory storage.
         */
        HYBRID,

        /**
         * Cache error.
         */
        ERROR,

    }

}
