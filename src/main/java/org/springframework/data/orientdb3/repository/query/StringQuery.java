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

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an OGM query. Can hold either cypher queries or filter definitions. Also in charge of adding pagination /
 * sort to the string based queries as OGM does not support pagination and sort on those.
 *
 * @author xxcxy
 */
// TODO
public class StringQuery {

    private static final String SKIP = "sdnSkip";
    private static final String LIMIT = "sdnLimit";
    private static final String SKIP_LIMIT = " SKIP {" + SKIP + "} LIMIT {" + LIMIT + "}";
    private static final String ORDER_BY_CLAUSE = " ORDER BY %s";

    private String sql;
    private Map<String, Object> parameters;
    private String countQuery;

    public StringQuery(String sql, Map<String, Object> parameters) {
        Assert.notNull(sql, "StringQuery must not be null.");
        Assert.notNull(parameters, "Parameters must not be null.");
        this.sql = sanitize(sql);
        this.parameters = parameters;
    }

    public StringQuery(String sql, String countQuery, Map<String, Object> parameters) {
        Assert.notNull(sql, "StringQuery must not be null.");
        Assert.notNull(parameters, "Parameters must not be null.");
        this.sql = sanitize(sql);
        this.countQuery = sanitize(countQuery);
        this.parameters = parameters;
    }

    public String getSql() {
        return sql;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getCountQuery() {
        return countQuery;
    }

    public String getSql(Pageable pageable, boolean forSlicing) {
        String result = sql;
        Sort sort = null;
        if (pageable.isPaged() && pageable.getSort() != Sort.unsorted()) {
            sort = pageable.getSort();
        }
        if (sort != Sort.unsorted()) {
            result = addSorting(result, sort);
        }
        result = addPaging(result, pageable, forSlicing);
        return result;
    }

    public String getCypherQuery(Sort sort) {
        // Custom queries in the OGM do not support pageable
        String result = sql;
        if (sort != Sort.unsorted()) {
            result = addSorting(sql, sort);
        }
        return result;
    }

    private String addPaging(String cypherQuery, Pageable pageable, boolean forSlicing) {
        // Custom queries in the OGM do not support pageable
        cypherQuery = formatBaseQuery(cypherQuery);
        cypherQuery = cypherQuery + SKIP_LIMIT;
        parameters.put(SKIP, pageable.getPageNumber() * pageable.getPageSize());
        if (forSlicing) {
            parameters.put(LIMIT, pageable.getPageSize() + 1);
        } else {
            parameters.put(LIMIT, pageable.getPageSize());
        }
        return cypherQuery;
    }

    private String addSorting(String baseQuery, Sort sort) {
        baseQuery = formatBaseQuery(baseQuery);
        if (sort == null) {
            return baseQuery;
        }
        final String sortOrder = getSortOrder(sort);
        if (sortOrder.isEmpty()) {
            return baseQuery;
        }
        return baseQuery + String.format(ORDER_BY_CLAUSE, sortOrder);
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
