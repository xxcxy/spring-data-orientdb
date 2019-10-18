package org.springframework.data.repository.orientdb3.repository.exception;

public class EntityConvertException extends RuntimeException {
    public EntityConvertException(final String message) {
        super(message);
    }
}
