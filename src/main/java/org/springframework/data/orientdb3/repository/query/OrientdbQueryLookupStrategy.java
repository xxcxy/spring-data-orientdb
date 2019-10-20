package org.springframework.data.orientdb3.repository.query;

import org.springframework.data.orientdb3.repository.Query;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

public final class OrientdbQueryLookupStrategy {

    /**
     * Private constructor to prevent instantiation.
     */
    private OrientdbQueryLookupStrategy() {
    }

    private abstract static class AbstractQueryLookupStrategy implements QueryLookupStrategy {

        private final OrientdbEntityManager em;

        /**
         * Creates a new {@link AbstractQueryLookupStrategy}.
         *
         * @param em
         */
        public AbstractQueryLookupStrategy(final OrientdbEntityManager em) {
            this.em = em;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
         */
        @Override
        public final RepositoryQuery resolveQuery(final Method method, final RepositoryMetadata metadata,
                                                  final ProjectionFactory factory, final NamedQueries namedQueries) {
            return resolveQuery(new OrientdbQueryMethod(method, metadata, factory), em, namedQueries);
        }

        protected abstract AbstractQuery resolveQuery(final OrientdbQueryMethod method,
                                                      final OrientdbEntityManager em,
                                                      final NamedQueries namedQueries);
    }

    private static class CreateQueryLookupStrategy extends AbstractQueryLookupStrategy {

        public CreateQueryLookupStrategy(final OrientdbEntityManager em) {
            super(em);
        }

        @Override
        protected CreateQuery resolveQuery(final OrientdbQueryMethod method, final OrientdbEntityManager em,
                                           final NamedQueries namedQueries) {
            return new CreateQuery(method, em, namedQueries);
        }
    }

    /**
     * {@link QueryLookupStrategy} that tries to detect a declared query declared via {@link Query} annotation followed by
     * a Orientdb named query lookup.
     */
    private static class DeclaredQueryLookupStrategy extends AbstractQueryLookupStrategy {

        private final QueryMethodEvaluationContextProvider evaluationContextProvider;

        /**
         * Creates a new {@link DeclaredQueryLookupStrategy}.
         *
         * @param em
         * @param evaluationContextProvider
         */
        public DeclaredQueryLookupStrategy(final OrientdbEntityManager em,
                                           final QueryMethodEvaluationContextProvider evaluationContextProvider) {
            super(em);
            this.evaluationContextProvider = evaluationContextProvider;
        }

        @Override
        protected DeclaredQuery resolveQuery(final OrientdbQueryMethod method, final OrientdbEntityManager em,
                                             final NamedQueries namedQueries) {

            return new DeclaredQuery(method, em, namedQueries, evaluationContextProvider);
        }
    }

    /**
     * {@link QueryLookupStrategy} to try to detect a declared query first (
     * {@link Query}, Orientdb named query). In case none is found we fall back on
     * query creation.
     */
    private static class CreateIfNotFoundQueryLookupStrategy extends AbstractQueryLookupStrategy {

        private final DeclaredQueryLookupStrategy lookupStrategy;
        private final CreateQueryLookupStrategy createStrategy;

        /**
         * Creates a new {@link CreateIfNotFoundQueryLookupStrategy}.
         *
         * @param em
         * @param createStrategy
         * @param lookupStrategy
         */
        public CreateIfNotFoundQueryLookupStrategy(final OrientdbEntityManager em,
                                                   final CreateQueryLookupStrategy createStrategy,
                                                   final DeclaredQueryLookupStrategy lookupStrategy) {

            super(em);
            this.createStrategy = createStrategy;
            this.lookupStrategy = lookupStrategy;
        }

        @Override
        protected AbstractQuery resolveQuery(final OrientdbQueryMethod method,
                                             final OrientdbEntityManager em,
                                             final NamedQueries namedQueries) {
            try {
                return lookupStrategy.resolveQuery(method, em, namedQueries);
            } catch (IllegalStateException e) {
                return createStrategy.resolveQuery(method, em, namedQueries);
            }
        }
    }

    /**
     * Creates a {@link QueryLookupStrategy} for the given {@link OrientdbEntityManager} and {@link Key}.
     *
     * @param em                        must not be {@literal null}.
     * @param key                       may be {@literal null}.
     * @param evaluationContextProvider must not be {@literal null}.
     * @return
     */
    public static QueryLookupStrategy create(final OrientdbEntityManager em, final Key key,
                                             final QueryMethodEvaluationContextProvider evaluationContextProvider) {

        Assert.notNull(em, "EntityManager must not be null!");
        Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");

        switch (key != null ? key : Key.CREATE_IF_NOT_FOUND) {
            case CREATE:
                return new CreateQueryLookupStrategy(em);
            case USE_DECLARED_QUERY:
                return new DeclaredQueryLookupStrategy(em, evaluationContextProvider);
            case CREATE_IF_NOT_FOUND:
                return new CreateIfNotFoundQueryLookupStrategy(em,
                        new CreateQueryLookupStrategy(em),
                        new DeclaredQueryLookupStrategy(em, evaluationContextProvider));
            default:
                throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
        }
    }
}
