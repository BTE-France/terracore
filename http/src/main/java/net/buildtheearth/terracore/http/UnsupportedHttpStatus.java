package net.buildtheearth.terracore.http;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Indicates that an unsupported HTTP status code was encountered.
 *
 * @author Smyler
 */
@Accessors(fluent = true)
@Getter
public class UnsupportedHttpStatus extends RuntimeException {

    /**
     * The unsupported HTTP status code.
     */
    private final int statusCode;

    public UnsupportedHttpStatus(int statusCode) {
        super("Unsupported HTTP status code: " + statusCode);
        this.statusCode = statusCode;
    }

}
