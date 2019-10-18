package org.springframework.data.repository.orientdb3.repository.query;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;

public class OrientdbQueryMethod extends QueryMethod {

    public OrientdbQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
    }
}
