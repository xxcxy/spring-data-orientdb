package org.springframework.data.repository.orientdb3.repository.query;

import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

public abstract class AbstractQuery implements RepositoryQuery {

    private final OrientdbQueryMethod method;


    public AbstractQuery(final OrientdbQueryMethod method) {
        Assert.notNull(method, "OrientdbQueryMethod must not be null!");

        this.method = method;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.RepositoryQuery#getQueryMethod()
     */
    @Override
    public OrientdbQueryMethod getQueryMethod() {
        return method;
    }

}
