package org.springframework.data.orientdb3.repository.exception;

public class EntityInitException extends RuntimeException {
    public EntityInitException(final String message) {
        super(message);
    }
}
