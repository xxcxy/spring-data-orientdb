package org.springframework.data.orientdb3.repository.query;

import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

public class DeclaredQuery extends AbstractQuery {
    public DeclaredQuery(final OrientdbQueryMethod method,
                         final OrientdbEntityManager em,
                         final NamedQueries namedQueries,
                         final QueryMethodEvaluationContextProvider evaluationContextProvider) {
        super(method);
    }

    @Override
    public Object execute(final Object[] objects) {
        return null;
    }
}
