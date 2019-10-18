
package org.springframework.data.repository.orientdb3.transaction;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.repository.orientdb3.support.SessionFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class OrientdbTransactionManager extends AbstractPlatformTransactionManager
        implements ResourceTransactionManager, BeanFactoryAware, InitializingBean {

    private SessionFactory sessionFactory;

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

    @Override
    public void afterPropertiesSet() {
        if (getSessionFactory() == null) {
            throw new IllegalArgumentException("'sessionFactory' is required");
        }
    }

    @Override
    public Object getResourceFactory() {
        return getSessionFactory();
    }

    @Override
    protected Object doGetTransaction() {
        Object sessionHolder = TransactionSynchronizationManager.getResource(getSessionFactory());
        if (sessionHolder != null) {
            return sessionHolder;
        }
        return new SessionHolder();
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) {
        SessionHolder sh = (SessionHolder) transaction;
        return sh.getSession() != null && sh.getSession().getTransaction().isActive();
    }

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

    @Override
    protected Object doSuspend(Object transaction) {
        return new SuspendedResourcesHolder((SessionHolder) TransactionSynchronizationManager
                .unbindResource(getSessionFactory()));
    }

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

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        SessionHolder txObject = (SessionHolder) status.getTransaction();
        txObject.getSession().commit();
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        SessionHolder txObject = (SessionHolder) status.getTransaction();
        txObject.getSession().rollback();
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        status.setRollbackOnly();
    }

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

        private SuspendedResourcesHolder(SessionHolder sessionHolder) {
            this.sessionHolder = sessionHolder;
        }

        private SessionHolder getSessionHolder() {
            return this.sessionHolder;
        }
    }
}
