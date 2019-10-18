package org.springframework.data.repository.orientdb3.repository.support;

import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.orientdb3.repository.OrientdbRepository;
import org.springframework.data.repository.orientdb3.repository.query.OrientdbQueryLookupStrategy;
import org.springframework.data.repository.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Optional;

public class OrientdbRepositoryFactory extends RepositoryFactorySupport {

    private final OrientdbEntityManager entityManager;
    private final OrientdbIdParserHolder orientdbIdParserHolder;


    public OrientdbRepositoryFactory(final OrientdbEntityManager entityManager,
                                     final OrientdbIdParserHolder orientdbIdParserHolder) {
        Assert.notNull(entityManager, "EntityManager must not be null!");

        this.entityManager = entityManager;
        this.orientdbIdParserHolder = orientdbIdParserHolder;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getTargetRepository(org.springframework.data.repository.core.RepositoryMetadata)
     */
    @Override
    protected final OrientdbRepository<?, ?> getTargetRepository(RepositoryInformation information) {
        OrientdbEntityInformation entityInformation = getEntityInformation(information.getDomainType());
        Object repository = getTargetRepositoryViaReflection(information, entityInformation, entityManager);

        Assert.isInstanceOf(OrientdbRepository.class, repository);

        return (OrientdbRepository<?, ?>) repository;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getRepositoryBaseClass(org.springframework.data.repository.core.RepositoryMetadata)
     */
    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleOrientdbRepository.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getQueryLookupStrategy(org.springframework.data.repository.query.QueryLookupStrategy.Key, org.springframework.data.repository.query.EvaluationContextProvider)
     */
    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable QueryLookupStrategy.Key key,
                                                                   QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional
                .of(OrientdbQueryLookupStrategy.create(entityManager, key, evaluationContextProvider));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getEntityInformation(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, ID> OrientdbEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return new OrientdbEntityInformation<>(domainClass, orientdbIdParserHolder);
    }

}
