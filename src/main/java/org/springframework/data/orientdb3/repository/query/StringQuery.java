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
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Represents an OGM query. Can hold either cypher queries or filter definitions. Also in charge of adding pagination /
 * sort to the string based queries as OGM does not support pagination and sort on those.
 *
 * @author xxcxy
 */
public class StringQuery {

    private static final String SKIP_LIMIT = " SKIP ? LIMIT ? ";
    private static final String ORDER_BY_CLAUSE = " ORDER BY ";

    private String sql;
    private List<Object> parameters;
    private String countQuery;

    public StringQuery(String sql, Object[] parameters) {
        this(sql, null, parameters);
    }

    public StringQuery(String sql, String countQuery, Object[] parameters) {
        Assert.notNull(sql, "StringQuery must not be null.");
        Assert.notNull(parameters, "Parameters must not be null.");
        this.sql = sanitize(sql);
        this.countQuery = sanitize(countQuery);
        this.parameters = new ArrayList<>(asList(parameters));
    }

    public String getSql() {
        return sql;
    }

    public Object[] getParameters() {
        return parameters.toArray();
    }

    public String getCountQuery() {
        return countQuery;
    }

    public String getSql(Pageable pageable, boolean forSlicing) {
        String result = sql;
        if (pageable.isPaged() && pageable.getSort() != null && pageable.getSort() != Sort.unsorted()) {
            result = addSorting(result, pageable.getSort());
        }
        result = addPaging(result, pageable, forSlicing);
        return result;
    }

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

    private int getParameterInsertIndex() {
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i) instanceof Pageable || parameters.get(i) instanceof Sort) {
                return i;
            }
        }
        return parameters.size();
    }

    private String addSorting(final String baseQuery, final Sort sort) {
        final String sortOrder = getSortOrder(sort);
        if (sortOrder.isEmpty()) {
            return baseQuery;
        }
        return String.join(ORDER_BY_CLAUSE, formatBaseQuery(baseQuery), sortOrder);
    }

    private String getSortOrder(Sort sort) {
        return sort.stream()
                .map(order -> order.getProperty() + " " + order.getDirection())
                .collect(Collectors.joining(", "));
    }

    private String formatBaseQuery(String sql) {
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        return sql;
    }

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
