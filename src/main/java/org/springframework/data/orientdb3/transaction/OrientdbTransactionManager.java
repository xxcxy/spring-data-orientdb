
package org.springframework.data.orientdb3.transaction;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.orientdb3.support.SessionFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Orientdb specific generic transaction manager.
 *
 * @author xxcxy
 */
public class OrientdbTransactionManager extends AbstractPlatformTransactionManager
        implements ResourceTransactionManager, BeanFactoryAware, InitializingBean {

    private SessionFactory sessionFactory;

    /**
     * Creates a new {@link OrientdbTransactionManager}.
     *
     * @param sessionFactory
     */
    public OrientdbTransactionManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Set the SessionFactory that this instance should manage transactions for.
     * <p>
     * By default, a default SessionFactory will be retrieved by finding a single unique bean of type SessionFactory in
     * the containing BeanFactory.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Return the SessionFactory that this instance should manage transactions for.
     */
    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    /**
     * Retrieves a default SessionFactory bean.
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (getSessionFactory() == null) {
            setSessionFactory(beanFactory.getBean(SessionFactory.class));
        }
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        if (getSessionFactory() == null) {
            throw new IllegalArgumentException("'sessionFactory' is required");
        }
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#getResourceFactory()
     */
    @Override
    public Object getResourceFactory() {
        return getSessionFactory();
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#doGetTransaction()
     */
    @Override
    protected Object doGetTransaction() {
        Object sessionHolder = TransactionSynchronizationManager.getResource(getSessionFactory());
        if (sessionHolder != null) {
            return sessionHolder;
        }
        return new SessionHolder();
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#isExistingTransaction()
     */
    @Override
    protected boolean isExistingTransaction(Object transaction) {
        SessionHolder sh = (SessionHolder) transaction;
        return sh.getSession() != null && sh.getSession().getTransaction().isActive();
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#doBegin()
     */
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        SessionHolder txObject = (SessionHolder) transaction;

        if (txObject.getSession() == null || txObject.isSynchronizedWithTransaction()) {
            ODatabaseSession session = sessionFactory.openSession();
            txObject.setSession(session);
            TransactionSynchronizationManager.bindResource(getSessionFactory(), txObject);
        }

        ODatabaseSession session = txObject.getSession();

        session.begin();
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#doSuspend()
     */
    @Override
    protected Object doSuspend(Object transaction) {
        return new SuspendedResourcesHolder((SessionHolder) TransactionSynchronizationManager
                .unbindResource(getSessionFactory()));
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#doResume()
     */
    @Override
    protected void doResume(Object transaction, Object suspendedResources) {
        SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
        if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
            // From non-transactional code running in active transaction synchronization
            // -> can be safely removed, will be closed on transaction completion.
            TransactionSynchronizationManager.unbindResource(getSessionFactory());
        }
        TransactionSynchronizationManager.bindResource(getSessionFactory(), resourcesHolder.getSessionHolder());
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#doCommit()
     */
    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        SessionHolder txObject = (SessionHolder) status.getTransaction();
        txObject.getSession().commit();
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#doRollback()
     */
    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        SessionHolder txObject = (SessionHolder) status.getTransaction();
        txObject.getSession().rollback();
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#doSetRollbackOnly()
     */
    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        status.setRollbackOnly();
    }

    /*
     * (non-Javadoc)
     * @see AbstractPlatformTransactionManager#doCleanupAfterCompletion()
     */
    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        SessionHolder txObject = (SessionHolder) transaction;
        TransactionSynchronizationManager.unbindResourceIfPossible(getSessionFactory());
        txObject.clear();
    }

    /**
     * Holder for suspended resources. Used internally by {@code doSuspend} and {@code doResume}.
     */
    private static class SuspendedResourcesHolder {

        private final SessionHolder sessionHolder;

        /**
         * Creates a new {@link SuspendedResourcesHolder}.
         *
         * @param sessionHolder
         */
        private SuspendedResourcesHolder(SessionHolder sessionHolder) {
            this.sessionHolder = sessionHolder;
        }

        /**
         * Gets the {@link SessionHolder}.
         *
         * @return
         */
        private SessionHolder getSessionHolder() {
            return this.sessionHolder;
        }
    }
}
