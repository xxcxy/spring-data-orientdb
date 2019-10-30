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

import org.springframework.data.domain.Sort;
import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.joining;

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
                processor.getReturnedType().getReturnedType(), queryMethod.getEntityInformation());

        return processor.processResult(results);
    }

    @Override
    protected StringQuery getQuery(final Object[] parameters) {
        StringBuilder sql = new StringBuilder(createBaseSql());
        sql.append(" where ");
        sql.append(getWhereClause(parameters));
        if (!queryMethod.isPageQuery() && !queryMethod.isSliceQuery()) {
            sql.append(" ").append(getSort()).append(getLimit());
        }
        return new StringQuery(sql.toString(), parameters);
    }

    private String createBaseSql() {
        if (tree.isDelete()) {
            throw new UnsupportedOperationException("Deleting has not been supported");
        }
        String entityName = queryMethod.getEntityInformation().getEntityName();
        if (tree.isCountProjection()) {
            return "select count(*) as count from ".concat(entityName);
        }
        if (tree.isExistsProjection()) {
            return "select 1 from ".concat(entityName);
        }
        return "select from ".concat(entityName);
    }

    private String getWhereClause(final Object[] parameters) {
        List<String> orParts = new ArrayList<>();
        Iterator<PartTree.OrPart> orPartIterator = tree.iterator();
        int parameterIndex = 0;
        while (orPartIterator.hasNext()) {
            parameterIndex = convertOrPart(orPartIterator.next(), orParts, parameterIndex, parameters);
        }
        return orParts.stream().collect(joining(" or "));
    }

    private int convertOrPart(final PartTree.OrPart orPart, final List<String> orParts,
                              final int parameterIndex, final Object[] parameters) {
        int newIndex = parameterIndex;
        List<String> andString = new ArrayList<>();
        Iterator<Part> partIterator = orPart.iterator();
        while (partIterator.hasNext()) {
            newIndex = convertPart(partIterator.next(), andString, newIndex, parameters);
        }
        orParts.add(andString.stream().collect(joining(" and ", "(", ")")));
        return newIndex;
    }

    private int convertPart(final Part part, final List<String> andString,
                            final int parameterIndex, final Object[] parameters) {
        Part.Type type = part.getType();
        String key = part.getProperty().toDotPath();
        if (type == Part.Type.BETWEEN) {
            andString.add(" " + key + " BETWEEN ? and ? ");
        } else if (type == Part.Type.AFTER) {
            andString.add(" " + key + " > ? ");
        } else if (type == Part.Type.BEFORE) {
            andString.add(" " + key + " < ? ");
        } else if (type == Part.Type.CONTAINING) {

        }
        return parameterIndex + part.getNumberOfArguments();
    }

    private String getSort() {
        Sort sort = tree.getSort();
        if (sort != null && !sort.isUnsorted()) {
            return sort.stream()
                    .map(order -> order.getProperty() + " " + order.getDirection())
                    .collect(joining(", "));
        }
        return "";
    }

    private String getLimit() {
        Integer limit = tree.getMaxResults();
        if (limit != null) {
            return " limit ".concat(limit.toString());
        }
        return "";
    }
}
