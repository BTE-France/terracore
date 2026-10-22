package net.buildtheearth.terracore.http;

import lombok.Builder;

/**
 * Options that may be passed to an {@link HttpClient} when making requests.
 *
 * @param readFromCache   whether the client should attempt to use its cache
 * @param writeToCache    whether the client should attempt to write to its cache
 */
@Builder(toBuilder = true)
public record RequestOptions(
        boolean readFromCache,
        boolean writeToCache
) {

    public static final RequestOptions DEFAULT = RequestOptions.builder().build();

    /**
     * Constructor for {@link RequestOptions}.
     *
     * @param readFromCache   whether the client should attempt to use its cache
     * @param writeToCache    whether the client should attempt to write to its cache
     *
     * @see RequestOptionsBuilder
     */
    public RequestOptions { }

    /**
     * A builder for {@link RequestOptions}.
     */
    @SuppressWarnings({ "unused", "FieldMayBeFinal" })  // They are used by the Lombok methods
    public static class RequestOptionsBuilder {
        private boolean readFromCache = true;
        private boolean writeToCache = true;
    }

}
