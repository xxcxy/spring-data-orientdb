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
import java.util.Collection;
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

    /**
     * Creates a new {@link PartTreeOrientdbQuery}.
     *
     * @param method
     * @param em
     * @param namedQueries
     */
    public PartTreeOrientdbQuery(final OrientdbQueryMethod method, final OrientdbEntityManager em,
                                 final NamedQueries namedQueries) {
        super(method, em, namedQueries);
        Class<?> domainType = method.getEntityInformation().getJavaType();
        this.tree = new PartTree(method.getName(), domainType);
    }

    /*
     * (non-Javadoc)
     * @see AbstractOrientdbRepositoryQuery#doExecute(java.lang.Object[])
     */
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

    /*
     * (non-Javadoc)
     * @see AbstractOrientdbRepositoryQuery#getQuery(java.lang.Object[])
     */
    @Override
    protected StringQuery getQuery(final Object[] parameters) {
        String where = getWhereClause(parameters);
        String entityName = queryMethod.getEntityInformation().getEntityName();

        StringBuilder sql = new StringBuilder(createBaseSql(entityName));
        sql.append(" where ");
        sql.append(where);
        if (!queryMethod.isPageQuery() && !queryMethod.isSliceQuery()) {
            sql.append(" ").append(getSort()).append(getLimit());
        }
        return new StringQuery(sql.toString(), createCountSql(entityName, where), parameters);
    }

    /**
     * Creates a base sql.
     *
     * @param entityName must not be {@literal null}.
     * @return
     */
    private String createBaseSql(final String entityName) {
        if (tree.isDelete()) {
            throw new UnsupportedOperationException("Deleting has not been supported");
        }
        if (tree.isCountProjection()) {
            return "select count(*) as count from ".concat(entityName);
        }
        if (tree.isExistsProjection()) {
            return "select 1 from ".concat(entityName);
        }
        return "select from ".concat(entityName);
    }

    /**
     * Creates a count sql.
     *
     * @param entityName must not be {@literal null}.
     * @param where      must not be {@literal null}.
     * @return
     */
    private String createCountSql(final String entityName, final String where) {
        return "select count(*) as count from ".concat(entityName).concat(" where ").concat(where);
    }

    /**
     * Creates a where clause sql.
     *
     * @param parameters must not be {@literal null}.
     * @return
     */
    private String getWhereClause(final Object[] parameters) {
        List<String> orParts = new ArrayList<>();
        Iterator<PartTree.OrPart> orPartIterator = tree.iterator();
        int parameterIndex = 0;
        while (orPartIterator.hasNext()) {
            parameterIndex = convertOrPart(orPartIterator.next(), orParts, parameterIndex, parameters);
        }
        return orParts.stream().collect(joining(" or "));
    }

    /**
     * Converts orPart to string sql.
     *
     * @param orPart         must not be {@literal null}.
     * @param orParts        must not be {@literal null}.
     * @param parameterIndex must not be {@literal null}.
     * @param parameters     must not be {@literal null}.
     * @return
     */
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

    /**
     * Converts part to string sql.
     *
     * @param part           must not be {@literal null}.
     * @param andString      must not be {@literal null}.
     * @param parameterIndex must not be {@literal null}.
     * @param parameters     must not be {@literal null}.
     * @return
     */
    private int convertPart(final Part part, final List<String> andString,
                            final int parameterIndex, final Object[] parameters) {
        Part.Type type = part.getType();
        String key = queryMethod.getEntityInformation()
                .getPropertyHandler(part.getProperty().toDotPath()).getPropertyName();
        if (type == Part.Type.BETWEEN) {
            andString.add(" " + key + " BETWEEN ? and ? ");
        } else if (type == Part.Type.AFTER) {
            andString.add(" " + key + " > ? ");
        } else if (type == Part.Type.BEFORE) {
            andString.add(" " + key + " < ? ");
        } else if (type == Part.Type.CONTAINING) {
            if (part.getProperty().getType().equals(String.class) && !part.getProperty().isCollection()) {
                andString.add(" " + key + " containsText ? ");
            } else if (parameters[parameterIndex] instanceof Collection) {
                andString.add(" " + key + " containsAny( ? ) ");
            } else {
                andString.add(" " + key + " contains( ? ) ");
            }
        } else if (type == Part.Type.IN) {
            andString.add(" " + key + " in ?");
        } else if (type == Part.Type.STARTING_WITH) {
            long length = parameters[parameterIndex].toString().length();
            andString.add(" " + key + ".left(" + length + ") = ? ");
        } else if (type == Part.Type.ENDING_WITH) {
            long length = parameters[parameterIndex].toString().length();
            andString.add(" " + key + ".right(" + length + ") = ? ");
        } else if (type == Part.Type.EXISTS || type == Part.Type.IS_NOT_NULL) {
            andString.add(" not(" + key + " is null) ");
        } else if (type == Part.Type.TRUE) {
            andString.add(" " + key + " = true ");
        } else if (type == Part.Type.FALSE) {
            andString.add(" " + key + " = false ");
        } else if (type == Part.Type.SIMPLE_PROPERTY) {
            andString.add(" " + key + " = ? ");
        } else if (type == Part.Type.IS_NULL) {
            andString.add(" " + key + " is null ");
        } else if (type == Part.Type.GREATER_THAN) {
            andString.add(" " + key + " > ? ");
        } else if (type == Part.Type.GREATER_THAN_EQUAL) {
            andString.add(" " + key + " >= ? ");
        } else if (type == Part.Type.LESS_THAN) {
            andString.add(" " + key + " < ? ");
        } else if (type == Part.Type.LESS_THAN_EQUAL) {
            andString.add(" " + key + " <= ? ");
        } else if (type == Part.Type.LIKE) {
            andString.add(" " + key + " like ? ");
        } else if (type == Part.Type.NOT_LIKE) {
            andString.add(" not(" + key + " like ?) ");
        } else if (type == Part.Type.REGEX) {
            andString.add(" " + key + " matches ? ");
        }
        return parameterIndex + part.getNumberOfArguments();
    }

    /**
     * Gets sort sql.
     *
     * @return
     */
    private String getSort() {
        Sort sort = tree.getSort();
        if (sort != null && !sort.isUnsorted()) {
            return sort.stream()
                    .map(order -> order.getProperty() + " " + order.getDirection())
                    .collect(joining(", "));
        }
        return "";
    }

    /**
     * Gets limit sql.
     *
     * @return
     */
    private String getLimit() {
        Integer limit = tree.getMaxResults();
        if (limit != null) {
            return " limit ".concat(limit.toString());
        }
        return "";
    }
}
