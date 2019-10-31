package org.springframework.data.orientdb3.repository.query;

import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;

/**
 * Implementation of {@link RepositoryQuery}.
 *
 * @author xxcxy
 */
public class DeclaredQuery extends AbstractOrientdbRepositoryQuery {

    private final String queryString;

    /**
     * Creates a new {@link DeclaredQuery}
     *
     * @param method
     * @param em
     * @param namedQueries
     */
    public DeclaredQuery(final OrientdbQueryMethod method,
                         final OrientdbEntityManager em,
                         final NamedQueries namedQueries) {
        super(method, em, namedQueries);
        queryString = queryMethod.getRequiredAnnotatedQuery();
    }

    /*
     * (non-Javadoc)
     * @see AbstractOrientdbRepositoryQuery#getQuery(java.lang.Object[])
     */
    @Override
    protected StringQuery getQuery(final Object[] parameters) {
        return new StringQuery(queryString, queryMethod.getCountQuery(),
                parameters);
    }

    /*
     * (non-Javadoc)
     * @see AbstractOrientdbRepositoryQuery#doExecute()
     */
    @Override
    protected Object doExecute(final StringQuery params, final Object[] parameters,
                               final OrientdbEntityInformation<?, ?> entityInformation) {
        ParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);
        ResultProcessor processor = queryMethod.getResultProcessor().withDynamicProjection(accessor);
        Object results = getExecution(accessor).execute(params,
                processor.getReturnedType().getReturnedType(), queryMethod.getEntityInformation());
        return processor.processResult(results);
    }

}
