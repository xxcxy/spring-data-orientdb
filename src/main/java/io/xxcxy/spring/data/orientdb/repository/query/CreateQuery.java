package io.xxcxy.spring.data.orientdb.repository.query;

import io.xxcxy.spring.data.orientdb.support.OrientdbEntityManager;
import org.springframework.data.repository.core.NamedQueries;

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
