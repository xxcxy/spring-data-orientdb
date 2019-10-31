/*
 * Copyright 2011-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.orientdb3.repository.query;

import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;

import java.util.EmptyStackException;

/**
 * Abstract base class to implement {@link RepositoryQuery}s.
 *
 * @author xxcxy
 */
public abstract class AbstractOrientdbRepositoryQuery implements RepositoryQuery {

    protected final OrientdbQueryMethod queryMethod;
    protected final OrientdbEntityManager em;
    protected final NamedQueries namedQueries;

    /**
     * Creates a new {@link AbstractOrientdbRepositoryQuery} from the given {@link OrientdbQueryMethod}.
     *
     * @param method
     * @param em
     * @param namedQueries
     */
    protected AbstractOrientdbRepositoryQuery(final OrientdbQueryMethod method, final OrientdbEntityManager em,
                                              final NamedQueries namedQueries) {

        this.queryMethod = method;
        this.em = em;
        this.namedQueries = namedQueries;
    }

    /**
     * Returns a {@link StringQuery}
     *
     * @param parameters
     * @return
     */
    protected abstract StringQuery getQuery(Object[] parameters);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.RepositoryQuery#execute(java.lang.Object[])
     */
    @Override
    public Object execute(Object[] parameters) {

        StringQuery stringQuery;
        try {
            stringQuery = getQuery(parameters);
        } catch (EmptyStackException e) {
            throw new IllegalArgumentException("Not enough arguments for stringQuery " + getQueryMethod().getName());
        }

        return doExecute(stringQuery, parameters, queryMethod.getEntityInformation());
    }

    /**
     * Executes a query.
     *
     * @param params            must not be {@literal null}.
     * @param parameters        must not be {@literal null}.
     * @param entityInformation must not be {@literal null}.
     * @return
     */
    protected abstract Object doExecute(StringQuery params, Object[] parameters,
                                        OrientdbEntityInformation<?, ?> entityInformation);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.RepositoryQuery#getQueryMethod()
     */
    @Override
    public OrientdbQueryMethod getQueryMethod() {
        return queryMethod;
    }

    /**
     * Returns a {@link OrientdbQueryExecution}
     *
     * @param accessor must not be {@literal null}.
     * @return
     */
    protected OrientdbQueryExecution getExecution(ParameterAccessor accessor) {
        if (queryMethod.isStreamQuery()) {
            return new OrientdbQueryExecution.CollectionExecution(em, accessor);
        }
        if (queryMethod.isModifyingQuery()) {
            return new OrientdbQueryExecution.ModifyingExecution(em, accessor);
        }
        if (queryMethod.isCollectionQuery()) {
            return new OrientdbQueryExecution.CollectionExecution(em, accessor);
        }
        if (queryMethod.isPageQuery()) {
            return new OrientdbQueryExecution.PagedExecution(em, accessor);
        }
        if (queryMethod.isSliceQuery()) {
            return new OrientdbQueryExecution.SlicedExecution(em, accessor);
        }
        return new OrientdbQueryExecution.SingleEntityExecution(em, accessor);
    }
}
