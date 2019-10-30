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
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.RepositoryQuery;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Base class for @link {@link RepositoryQuery}s.
 *
 * @author xxcxy
 */
public abstract class AbstractOrientdbRepositoryQuery implements RepositoryQuery {

    protected final OrientdbQueryMethod queryMethod;
    protected final OrientdbEntityManager em;
    protected final NamedQueries namedQueries;

    protected AbstractOrientdbRepositoryQuery(final OrientdbQueryMethod method, final OrientdbEntityManager em,
                                              final NamedQueries namedQueries) {

        this.queryMethod = method;
        this.em = em;
        this.namedQueries = namedQueries;
    }

    protected abstract StringQuery getQuery(Map<String, Object> parameters);

    @Override
    public Object execute(Object[] parameters) {

        StringQuery stringQuery;
        try {
            stringQuery = getQuery(getParameters(parameters));
        } catch (EmptyStackException e) {
            throw new IllegalArgumentException("Not enough arguments for stringQuery " + getQueryMethod().getName());
        }

        return doExecute(stringQuery, parameters, queryMethod.getEntityInformation());
    }

    private Map<String, Object> getParameters(Object[] parameters) {
        Map<String, Object> map = new HashMap<>();
        Parameters<?, ?> methodParameters = queryMethod.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Optional<String> parameterName = methodParameters.getParameter(i).getName();
            if (parameterName.isPresent()) {
                map.put(parameterName.get(), parameters[i]);
            }
        }
        return map;
    }

    protected abstract Object doExecute(StringQuery params, Object[] parameters,
                                        OrientdbEntityInformation<?, ?> entityInformation);

    @Override
    public OrientdbQueryMethod getQueryMethod() {
        return queryMethod;
    }

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
