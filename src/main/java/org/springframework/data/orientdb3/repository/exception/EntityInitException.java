package org.springframework.data.orientdb3.repository.exception;

/**
 * Signals that we encountered an invalid Entity when Init a
 * {@link org.springframework.data.repository.core.EntityInformation}.
 *
 * @author xxcxy
 */
public class EntityInitException extends RuntimeException {
    /**
     * Creates a new {@link EntityConvertException} with the given message.
     *
     * @param message must not be {@literal null} or empty.
     */
    public EntityInitException(final String message) {
        super(message);
    }
}
