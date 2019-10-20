package org.springframework.data.orientdb3.repository.support;

import org.springframework.data.orientdb3.repository.EdgeEntity;
import org.springframework.data.orientdb3.repository.ElementEntity;
import org.springframework.data.orientdb3.repository.VertexEntity;

import java.util.Optional;

public enum EntityType {
    ELEMENT, VERTEX, EDGE;

    public static <T> Optional<EntityType> getEntityType(final Class<T> clazz) {
        if (clazz.getAnnotation(ElementEntity.class) != null) {
            return Optional.of(ELEMENT);
        }
        if (clazz.getAnnotation(VertexEntity.class) != null) {
            return Optional.of(VERTEX);
        }
        if (clazz.getAnnotation(EdgeEntity.class) != null) {
            return Optional.of(EDGE);
        }
        return Optional.empty();
    }
}
