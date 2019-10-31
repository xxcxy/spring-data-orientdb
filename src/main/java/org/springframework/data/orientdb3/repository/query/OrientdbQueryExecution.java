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

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Classes intended to pilot query execution according to the type of the query. The type of the query is determined by
 * looking at the result class of the method.
 *
 * @author xxcxy
 * @see AbstractOrientdbRepositoryQuery#getExecution(ParameterAccessor)
 */
public abstract class OrientdbQueryExecution {
    private final OrientdbEntityManager entityManager;
    private final ParameterAccessor accessor;

    /**
     * Creates a new {@ling OrientdbQueryExecution}.
     *
     * @param entityManager must not be {@literal null}.
     * @param accessor      must not be {@literal null}.
     */
    protected OrientdbQueryExecution(final OrientdbEntityManager entityManager, final ParameterAccessor accessor) {
        this.entityManager = entityManager;
        this.accessor = accessor;
    }

    /**
     * Executes the given {@link StringQuery} with the given {@link OrientdbEntityManager}.
     *
     * @param stringQuery       must not be {@literal null}.
     * @param type              must not be {@literal null}.
     * @param entityInformation must not be {@literal null}.
     * @return
     */
    public Object execute(final StringQuery stringQuery, final Class<?> type,
                          final OrientdbEntityInformation<?, ?> entityInformation) {
        return doExecute(entityManager, accessor, stringQuery, type, entityInformation);
    }

    /**
     * Executes the given sql with the given {@link OrientdbEntityManager}.
     *
     * @param sql               must not be {@literal null}.
     * @param parameters        must not be {@literal null}.
     * @param type              must not be {@literal null}.
     * @param entityInformation must not be {@literal null}.
     * @return
     */
    protected List<?> doQuery(final String sql, final Object[] parameters, final Class<?> type,
                              final OrientdbEntityInformation<?, ?> entityInformation) {
        if (type.equals(entityInformation.getJavaType())) {
            return entityManager.doQuery(sql, parameters, entityInformation);
        } else {
            return entityManager.doQuery(sql, parameters, type);
        }
    }

    /**
     * Method to implement {@link OrientdbQueryExecution} executions by single enum values.
     *
     * @param em                must not be {@literal null}.
     * @param accessor          must not be {@literal null}.
     * @param stringQuery       must not be {@literal null}.
     * @param type              must not be {@literal null}.
     * @param entityInformation must not be {@literal null}.
     * @return
     */
    protected abstract Object doExecute(OrientdbEntityManager em, ParameterAccessor accessor,
                                        StringQuery stringQuery, Class<?> type,
                                        OrientdbEntityInformation<?, ?> entityInformation);

    /**
     * Executes a {@link OrientdbQueryExecution} to return a single entity.
     */
    static final class SingleEntityExecution extends OrientdbQueryExecution {
        /**
         * Creates a new {@link SingleEntityExecution}.
         *
         * @param entityManager
         * @param accessor
         */
        SingleEntityExecution(OrientdbEntityManager entityManager, ParameterAccessor accessor) {
            super(entityManager, accessor);
        }

        /*
         * (non-Javadoc)
         * @see OrientdbQueryExecution.doExecute()
         */
        @Override
        public Object doExecute(final OrientdbEntityManager em, final ParameterAccessor accessor,
                                final StringQuery stringQuery, final Class<?> type,
                                final OrientdbEntityInformation<?, ?> entityInformation) {

            List<?> result = doQuery(stringQuery.getSql(), stringQuery.getParameters(), type, entityInformation);
            if (result.size() == 0) {
                return null;
            }
            if (result.size() > 1) {
                throw new IncorrectResultSizeDataAccessException("Incorrect result size: expected at most 1", 1);
            }
            return result.get(0);
        }
    }

    /**
     * Executes the query to return a simple collection of entities.
     */
    static final class CollectionExecution extends OrientdbQueryExecution {

        /**
         * Creates a new {@link CollectionExecution}.
         *
         * @param entityManager
         * @param accessor
         */
        CollectionExecution(OrientdbEntityManager entityManager, ParameterAccessor accessor) {
            super(entityManager, accessor);
        }

        /*
         * (non-Javadoc)
         * @see OrientdbQueryExecution.doExecute()
         */
        @Override
        protected Object doExecute(final OrientdbEntityManager em, final ParameterAccessor accessor,
                                   final StringQuery stringQuery, final Class<?> type,
                                   final OrientdbEntityInformation<?, ?> entityInformation) {
            return doQuery(stringQuery.getSql(), stringQuery.getParameters(), type, entityInformation);
        }
    }

    /**
     * Executes the {@link OrientdbQueryExecution} to return a {@link org.springframework.data.domain.Page} of
     * entities.
     */
    static final class PagedExecution extends OrientdbQueryExecution {
        /**
         * Creates a new {@link PagedExecution}.
         *
         * @param entityManager
         * @param accessor
         */
        PagedExecution(OrientdbEntityManager entityManager, ParameterAccessor accessor) {
            super(entityManager, accessor);
        }

        /*
         * (non-Javadoc)
         * @see OrientdbQueryExecution.doExecute()
         */
        @Override
        protected Object doExecute(final OrientdbEntityManager em, final ParameterAccessor accessor,
                                   final StringQuery stringQuery, final Class<?> type,
                                   final OrientdbEntityInformation<?, ?> entityInformation) {
            return PageableExecutionUtils.getPage(doQuery(stringQuery.getSql(accessor.getPageable(), false),
                    stringQuery.getParameters(), type, entityInformation),
                    accessor.getPageable(), () -> count(em, stringQuery));
        }

        private Long count(final OrientdbEntityManager em, final StringQuery stringQuery) {
            Assert.hasText(stringQuery.getCountQuery(), "Must specify a count stringQuery to get pagination info.");

            return em.doQueryCount(stringQuery.getCountQuery(), stringQuery.getParameters());
        }
    }

    /**
     * Executes the query to return a {@link Slice} of entities.
     */
    static final class SlicedExecution extends OrientdbQueryExecution {
        /**
         * Creates a new {@link SlicedExecution}.
         *
         * @param entityManager
         * @param accessor
         */
        SlicedExecution(OrientdbEntityManager entityManager, ParameterAccessor accessor) {
            super(entityManager, accessor);
        }

        /*
         * (non-Javadoc)
         * @see OrientdbQueryExecution.doExecute()
         */
        @Override
        protected Object doExecute(final OrientdbEntityManager em, final ParameterAccessor accessor,
                                   final StringQuery stringQuery, final Class<?> type,
                                   final OrientdbEntityInformation<?, ?> entityInformation) {
            Pageable pageable = accessor.getPageable();

            List<Object> resultList = (List<Object>) doQuery(stringQuery.getSql(pageable, true),
                    stringQuery.getParameters(), type, entityInformation);

            boolean hasNext = pageable.isPaged() && resultList.size() > pageable.getPageSize();

            return new SliceImpl<>(hasNext ? resultList.subList(0, pageable.getPageSize()) : resultList,
                    pageable, hasNext);
        }
    }

    /**
     * Executes a modifying query such as an update, insert or delete.
     */
    static final class ModifyingExecution extends OrientdbQueryExecution {
        /**
         * Creates a new {@link ModifyingExecution}.
         *
         * @param entityManager
         * @param accessor
         */
        ModifyingExecution(OrientdbEntityManager entityManager, ParameterAccessor accessor) {
            super(entityManager, accessor);
        }

        /*
         * (non-Javadoc)
         * @see OrientdbQueryExecution.doExecute()
         */
        @Override
        protected Object doExecute(final OrientdbEntityManager em, final ParameterAccessor accessor,
                                   final StringQuery stringQuery, final Class<?> type,
                                   final OrientdbEntityInformation<?, ?> entityInformation) {
            em.doCommand(stringQuery.getSql(), stringQuery.getParameters());
            return null;
        }
    }
}
