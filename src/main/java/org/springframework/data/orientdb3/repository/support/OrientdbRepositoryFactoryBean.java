package org.springframework.data.orientdb3.repository.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.orientdb3.support.SessionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.util.Assert;

/**
 * Special adapter for Springs {@link org.springframework.beans.factory.FactoryBean} interface to allow easy setup of
 * repository factories via Spring configuration.
 *
 * @param <T> the type of the repository
 * @author xxcxy
 */
public class OrientdbRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
        extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    private SessionFactory sessionFactory;
    private OrientdbIdParserHolder orientdbIdParserHolder;

    /**
     * Creates a new {@link OrientdbRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public OrientdbRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    /**
     * Sets the sessionFactory.
     *
     * @param sessionFactory must not be {@literal null}.
     */
    @Autowired
    public void setSessionFactory(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Sets the orientdbIdParserHolder.
     *
     * @param orientdbIdParserHolder must not be {@literal null}.
     */
    @Autowired
    public void setOrientdbIdParserHolder(final OrientdbIdParserHolder orientdbIdParserHolder) {
        this.orientdbIdParserHolder = orientdbIdParserHolder;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport#doCreateRepositoryFactory()
     */
    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {

        Assert.state(sessionFactory != null, "SessionFactory must not be null!");

        return createRepositoryFactory(sessionFactory);
    }

    /**
     * Returns a {@link RepositoryFactorySupport}.
     */
    protected RepositoryFactorySupport createRepositoryFactory(SessionFactory sessionFactory) {
        return new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory), orientdbIdParserHolder);
    }
}
