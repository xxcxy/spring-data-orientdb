package org.springframework.data.orientdb3.repository.query;

import org.springframework.data.orientdb3.repository.Query;
import org.springframework.data.orientdb3.repository.support.OrientdbIdParserHolder;
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

/**
 * StringQuery lookup strategy to execute finders.
 *
 * @author xxcxy
 */
public final class OrientdbQueryLookupStrategy {

    /**
     * Private constructor to prevent instantiation.
     */
    private OrientdbQueryLookupStrategy() {
    }

    /**
     * Base class for {@link QueryLookupStrategy} implementations.
     *
     * @author xxcxy
     */
    private abstract static class AbstractQueryLookupStrategy implements QueryLookupStrategy {

        private final OrientdbEntityManager em;
        private final OrientdbIdParserHolder orientdbIdParserHolder;

        /**
         * Creates a new {@link AbstractQueryLookupStrategy}.
         *
         * @param em
         */
        public AbstractQueryLookupStrategy(final OrientdbEntityManager em,
                                           final OrientdbIdParserHolder orientdbIdParserHolder) {
            this.em = em;
            this.orientdbIdParserHolder = orientdbIdParserHolder;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
         */
        @Override
        public final RepositoryQuery resolveQuery(final Method method, final RepositoryMetadata metadata,
                                                  final ProjectionFactory factory, final NamedQueries namedQueries) {
            return resolveQuery(new OrientdbQueryMethod(method, metadata, factory, orientdbIdParserHolder),
                    em, namedQueries);
        }

        protected abstract AbstractOrientdbRepositoryQuery resolveQuery(final OrientdbQueryMethod method,
                                                                        final OrientdbEntityManager em,
                                                                        final NamedQueries namedQueries);
    }

    /**
     * {@link QueryLookupStrategy} to create a query from the method name.
     *
     * @author xxcxy
     */
    private static class CreateQueryLookupStrategy extends AbstractQueryLookupStrategy {

        public CreateQueryLookupStrategy(final OrientdbEntityManager em, final OrientdbIdParserHolder orientdbIdParserHolder) {
            super(em, orientdbIdParserHolder);
        }

        @Override
        protected PartTreeOrientdbQuery resolveQuery(final OrientdbQueryMethod method, final OrientdbEntityManager em,
                                                     final NamedQueries namedQueries) {
            return new PartTreeOrientdbQuery(method, em, namedQueries);
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
                                           final QueryMethodEvaluationContextProvider evaluationContextProvider,
                                           final OrientdbIdParserHolder orientdbIdParserHolder) {
            super(em, orientdbIdParserHolder);
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
         * @param orientdbIdParserHolder
         * @param evaluationContextProvider
         */
        public CreateIfNotFoundQueryLookupStrategy(final OrientdbEntityManager em,
                                                   final OrientdbIdParserHolder orientdbIdParserHolder,
                                                   final QueryMethodEvaluationContextProvider
                                                           evaluationContextProvider) {

            super(em, orientdbIdParserHolder);
            this.createStrategy = new CreateQueryLookupStrategy(em, orientdbIdParserHolder);
            this.lookupStrategy = new DeclaredQueryLookupStrategy(em, evaluationContextProvider,
                    orientdbIdParserHolder);
        }

        @Override
        protected AbstractOrientdbRepositoryQuery resolveQuery(final OrientdbQueryMethod method,
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
                                             final QueryMethodEvaluationContextProvider evaluationContextProvider,
                                             final OrientdbIdParserHolder orientdbIdParserHolder) {

        Assert.notNull(em, "EntityManager must not be null!");
        Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");

        switch (key != null ? key : Key.CREATE_IF_NOT_FOUND) {
            case CREATE:
                return new CreateQueryLookupStrategy(em, orientdbIdParserHolder);
            case USE_DECLARED_QUERY:
                return new DeclaredQueryLookupStrategy(em, evaluationContextProvider, orientdbIdParserHolder);
            case CREATE_IF_NOT_FOUND:
                return new CreateIfNotFoundQueryLookupStrategy(em, orientdbIdParserHolder, evaluationContextProvider);
            default:
                throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
        }
    }
}
