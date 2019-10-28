package org.springframework.data.orientdb3.repository.query;

import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;

/**
 * Implementation of {@link RepositoryQuery}.
 *
 * @author xxcxy
 */
public class DeclaredQuery extends AbstractQuery {

    /**
     * Creates a new {@link DeclaredQuery}
     *
     * @param method
     * @param em
     * @param namedQueries
     * @param evaluationContextProvider
     */
    public DeclaredQuery(final OrientdbQueryMethod method,
                         final OrientdbEntityManager em,
                         final NamedQueries namedQueries,
                         final QueryMethodEvaluationContextProvider evaluationContextProvider) {
        super(method);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.RepositoryQuery#execute(java.lang.Object[])
     */
    @Override
    public Object execute(final Object[] objects) {
        return null;
    }
}
