package org.springframework.data.repository.orientdb3.repository.query;

import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.orientdb3.support.OrientdbEntityManager;

public class CreateQuery extends AbstractQuery {
    public CreateQuery(final OrientdbQueryMethod method, final OrientdbEntityManager em,
                       final NamedQueries namedQueries) {
        super(method);
    }

    @Override
    public Object execute(final Object[] objects) {
        return null;
    }
}
