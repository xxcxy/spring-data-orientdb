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

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Represents a {@link String} query. Hold sql queries . Also in charge of adding pagination /
 * sort to the string  queries.
 *
 * @author xxcxy
 */
public class StringQuery {

    private static final String SKIP_LIMIT = " SKIP ? LIMIT ? ";
    private static final String ORDER_BY_CLAUSE = " ORDER BY ";

    private String sql;
    private List<Object> parameters;
    private String countQuery;

    /**
     * Creates a new {@link StringQuery}.
     *
     * @param sql        must not be {@literal null}.
     * @param countQuery
     * @param parameters must not be {@literal null}.
     */
    public StringQuery(final String sql, @Nullable final String countQuery, final Object[] parameters) {
        Assert.notNull(sql, "StringQuery must not be null.");
        Assert.notNull(parameters, "Parameters must not be null.");
        this.sql = sanitize(sql);
        this.countQuery = sanitize(countQuery);
        this.parameters = new ArrayList<>(asList(parameters));
    }

    /**
     * Gets the string sql.
     *
     * @return
     */
    public String getSql() {
        return sql;
    }

    /**
     * Gets sql parameters.
     *
     * @return
     */
    public Object[] getParameters() {
        return parameters.toArray();
    }

    /**
     * Gets the count sql.
     *
     * @return
     */
    public String getCountQuery() {
        return countQuery;
    }

    /**
     * Gets a sql with given {@link Pageable}.
     *
     * @param pageable   must not be {@literal null}.
     * @param forSlicing must not be {@literal null}.
     * @return
     */
    public String getSql(final Pageable pageable, final boolean forSlicing) {
        String result = sql;
        if (pageable.isPaged() && pageable.getSort() != null && pageable.getSort() != Sort.unsorted()) {
            result = addSorting(result, pageable.getSort());
        }
        result = addPaging(result, pageable, forSlicing);
        return result;
    }

    /**
     * Gets a sql with given {@line Sort}
     *
     * @param sort
     * @return
     */
    public String getSql(final Sort sort) {
        return addSorting(sql, sort);
    }

    /**
     * Adds page sql to the source sql and new parameters.
     *
     * @param sql        must not be {@literal null}.
     * @param pageable   must not be {@literal null}.
     * @param forSlicing must not be {@literal null}.
     * @return
     */
    private String addPaging(String sql, Pageable pageable, boolean forSlicing) {
        int insertIndex = getParameterInsertIndex();
        parameters.add(insertIndex, pageable.getPageNumber() * pageable.getPageSize());
        if (forSlicing) {
            parameters.add(insertIndex + 1, pageable.getPageSize() + 1);
        } else {
            parameters.add(insertIndex + 1, pageable.getPageSize());
        }
        return formatBaseQuery(sql).concat(SKIP_LIMIT);
    }

    /**
     * Gets the new parameter index.
     *
     * @return
     */
    private int getParameterInsertIndex() {
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i) instanceof Pageable || parameters.get(i) instanceof Sort) {
                return i;
            }
        }
        return parameters.size();
    }

    /**
     * Returns a sql with given sort.
     *
     * @param baseQuery must not be {@literal null}.
     * @param sort      must not be {@literal null}.
     * @return
     */
    private String addSorting(final String baseQuery, final Sort sort) {
        final String sortOrder = getSortOrder(sort);
        if (sortOrder.isEmpty()) {
            return baseQuery;
        }
        return String.join(ORDER_BY_CLAUSE, formatBaseQuery(baseQuery), sortOrder);
    }

    /**
     * Gets sort string.
     *
     * @param sort must not be {@literal null}.
     * @return
     */
    private String getSortOrder(Sort sort) {
        return sort.stream()
                .map(order -> order.getProperty() + " " + order.getDirection())
                .collect(Collectors.joining(", "));
    }

    /**
     * Formats a sql.
     *
     * @param sql must not be {@literal null}.
     * @return
     */
    private String formatBaseQuery(String sql) {
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        return sql;
    }

    /**
     * Removes semicolon if the sql end with it.
     *
     * @param sql
     * @return
     */
    private String sanitize(String sql) {
        if (sql != null) {
            sql = sql.trim();
            if (sql.endsWith(";")) {
                sql = sql.substring(0, sql.length() - 1);
            }
        }
        return sql;
    }
}
