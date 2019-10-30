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
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.HashMap;
import java.util.Map;

/**
 * Specialisation of {@link RepositoryQuery} that handles mapping of filter finders.
 *
 * @author xxcxy
 */
public class PartTreeOrientdbQuery extends AbstractOrientdbRepositoryQuery {

    private final PartTree tree;

    public PartTreeOrientdbQuery(final OrientdbQueryMethod method, final OrientdbEntityManager em,
                                 final NamedQueries namedQueries) {
        super(method, em, namedQueries);
        Class<?> domainType = method.getEntityInformation().getJavaType();
        this.tree = new PartTree(method.getName(), domainType);
    }

    @Override
    protected Object doExecute(final StringQuery params, final Object[] parameters,
                               final OrientdbEntityInformation<?, ?> entityInformation) {

        ParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);
        Class<?> returnType = queryMethod.getReturnType();

        if (returnType.equals(Void.class)) {
            throw new RuntimeException("Derived Queries must have a return type");
        }

        ResultProcessor processor = queryMethod.getResultProcessor().withDynamicProjection(accessor);
        Object results = getExecution(accessor).execute(params,
                processor.getReturnedType().getDomainType(), queryMethod.getEntityInformation());

        return processor.processResult(results);
    }

    @Override
    protected StringQuery getQuery(Map<String, Object> parameters) {
        // TODO
        return new StringQuery("", new HashMap<>());
    }
}
