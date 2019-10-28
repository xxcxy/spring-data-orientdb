package org.springframework.data.orientdb3.repository.query;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;

/**
 * Orientdb specific extension of {@link QueryMethod}.
 *
 * @author xxcxy
 */
public class OrientdbQueryMethod extends QueryMethod {

    /**
     * Creates a new {@link OrientdbQueryMethod}.
     *
     * @param method   must not be {@literal null}
     * @param metadata must not be {@literal null}
     * @param factory  must not be {@literal null}
     */
    public OrientdbQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
    }
}
