package org.springframework.data.orientdb3.repository.exception;

/**
 * Signals that we encountered an invalid Entity when transforming.
 *
 * @author xxcxy
 */
public class EntityConvertException extends RuntimeException {

    /**
     * Creates a new {@link EntityConvertException} with the given message.
     *
     * @param message must not be {@literal null} or empty.
     */
    public EntityConvertException(final String message) {
        super(message);
    }
}
