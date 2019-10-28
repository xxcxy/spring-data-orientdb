package org.springframework.data.orientdb3.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.springframework.data.orientdb3.repository.query.TypedCommand;
import org.springframework.data.orientdb3.repository.query.TypedQuery;
import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;
import org.springframework.data.orientdb3.transaction.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * A orientdb entity manager.
 *
 * @author xxcxy
 */
public class OrientdbEntityManager {

    private final SessionFactory sessionFactory;

    /**
     * Creates a new {@link OrientdbEntityManager}.
     *
     * @param sessionFactory
     */
    public OrientdbEntityManager(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Saves an entity.
     *
     * @param entity
     * @param entityInformation
     * @param <T>
     * @param <ID>
     * @return
     */
    public <T, ID> T persist(final T entity, final OrientdbEntityInformation<T, ID> entityInformation) {
        return doWithSession(session -> {
            if (entity instanceof EntityProxyInterface) {
                ((EntityProxyInterface) entity).saveOElement(session, null);
                return entity;
            } else {
                return entityInformation.save(entity, session, null, new HashMap<>());
            }
        });
    }

    /**
     * Saves an entity to a designated cluster.
     *
     * @param entity
     * @param cluster
     * @param entityInformation
     * @param <T>
     * @param <ID>
     * @return
     */
    public <T, ID> T persist(final T entity, final String cluster,
                             final OrientdbEntityInformation<T, ID> entityInformation) {
        return doWithSession(session -> {
            if (entity instanceof EntityProxyInterface) {
                ((EntityProxyInterface) entity).saveOElement(session, cluster);
                return entity;
            } else {
                return entityInformation.save(entity, session, cluster, new HashMap<>());
            }
        });
    }

    /**
     * Deletes an entity from orientdb.
     *
     * @param entity
     * @param <T>
     */
    public <T> void remove(final T entity) {
        withSession(session -> {
            if (entity instanceof EntityProxyInterface) {
                ((EntityProxyInterface) entity).deleteOElement();
            }
        });
    }

    /**
     * Finds an entity for the given id.
     *
     * @param oId
     * @param entityInformation
     * @param <T>
     * @param <ID>
     * @return
     */
    public <T, ID> T find(final ID oId, final OrientdbEntityInformation<T, ID> entityInformation) {
        return doWithSession(session -> {
            OElement oElement = session.load(entityInformation.convertToORID(oId));
            if (oElement != null) {
                return entityInformation.getEntityProxy(oElement, new HashMap<>());
            } else {
                return null;
            }
        });
    }

    /**
     * Finds all entities by a designated class.
     *
     * @param entityInformation
     * @param <T>
     * @param <ID>
     * @return
     */
    public <T, ID> List<T> findAll(final OrientdbEntityInformation<T, ID> entityInformation) {
        List<T> all = new ArrayList<>();
        HashMap<OElement, Object> converted = new HashMap<>();
        withSession(session -> {
            ORecordIteratorClass<ODocument> oc = session.browseClass(entityInformation.getEntityName());
            while (oc.hasNext()) {
                all.add(entityInformation.getEntityProxy(oc.next(), converted));
            }
        });
        return all;
    }

    /**
     * Finds all entities by a designated cluster.
     *
     * @param clusterName
     * @param entityInformation
     * @param <T>
     * @param <ID>
     * @return
     */
    public <T, ID> List<T> findAll(final String clusterName,
                                   final OrientdbEntityInformation<T, ID> entityInformation) {
        List<T> all = new ArrayList<>();
        HashMap<OElement, Object> converted = new HashMap<>();
        withSession(session -> {
            for (ORecord oRecord : session.browseCluster(clusterName)) {
                all.add(entityInformation.getEntityProxy(oRecord.getRecord(), converted));
            }
        });
        return all;
    }

    /**
     * Counts a designated class.
     *
     * @param entityInformation
     * @param <T>
     * @param <ID>
     * @return
     */
    public <T, ID> Long count(final OrientdbEntityInformation<T, ID> entityInformation) {
        return doWithSession(session -> session.countClass(entityInformation.getEntityName()));
    }

    /**
     * Creates a {@link TypedQuery}.
     *
     * @param query
     * @param entityInformation
     * @param <T>
     * @param <ID>
     * @return
     */
    public <T, ID> TypedQuery<T, ID> createQuery(final String query,
                                                 final OrientdbEntityInformation<T, ID> entityInformation) {
        return doWithSession(session ->
                new TypedQuery<>(session.query(format(query, entityInformation.getEntityName())), entityInformation));
    }

    /**
     * Creates a {@link TypedCommand}.
     *
     * @param sql
     * @param entityInformation
     * @param <T>
     * @param <ID>
     */
    public <T, ID> void createCommand(final String sql, final OrientdbEntityInformation<T, ID> entityInformation) {
        withSession(session ->
                new TypedCommand<>(session.command(format(sql, entityInformation.getEntityName())), entityInformation));
    }

    /**
     * Wraps a session getter.
     *
     * @param function
     * @param <R>
     * @return
     */
    private <R> R doWithSession(final Function<ODatabaseSession, R> function) {
        Object sessionHolder = TransactionSynchronizationManager.getResource(sessionFactory);
        if (sessionHolder != null) {
            return function.apply(((SessionHolder) sessionHolder).getSession());
        } else {
            // If it is not in a transaction, every call use a independent session
            ODatabaseSession session = sessionFactory.openSession();
            R r = function.apply(session);
            session.close();
            return r;
        }
    }

    /**
     * Wraps a session getter.
     *
     * @param consumer
     */
    private void withSession(final Consumer<ODatabaseSession> consumer) {
        Object sessionHolder = TransactionSynchronizationManager.getResource(sessionFactory);
        if (sessionHolder != null) {
            consumer.accept(((SessionHolder) sessionHolder).getSession());
        } else {
            ODatabaseSession session = sessionFactory.openSession();
            consumer.accept(session);
            session.close();
        }
    }
}
