package org.springframework.data.repository.orientdb3.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.ORecord;
import org.springframework.data.repository.orientdb3.repository.query.TypedQuery;
import org.springframework.data.repository.orientdb3.repository.support.OrientdbEntityInformation;
import org.springframework.data.repository.orientdb3.transaction.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;

public class OrientdbEntityManager {

    private final SessionFactory sessionFactory;

    public OrientdbEntityManager(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public <T, ID> void persist(final T entity, final OrientdbEntityInformation<T, ID> entityInformation) {
        withSession(session -> {
            ORecord oRecord = entityInformation.convertToORecord(entity, session);
            session.save(oRecord);
            entityInformation.setId(entity, oRecord.getIdentity());
        });
    }

    public <T, ID> void persist(final T entity, final String cluster,
                                final OrientdbEntityInformation<T, ID> entityInformation) {
        withSession(session -> {
            ORecord oRecord = entityInformation.convertToORecord(entity, session);
            session.save(oRecord, cluster);
            entityInformation.setId(entity, oRecord.getIdentity());
        });
    }

    public <T, ID> void remove(final T entity, final OrientdbEntityInformation<T, ID> entityInformation) {
        withSession(session -> session.delete(entityInformation.convertToORecord(entity, session)));
    }

    public <T, ID> T find(final ID oId, final OrientdbEntityInformation<T, ID> entityInformation) {
        OElement oRecord = doWithSession(session -> session.load(entityInformation.convertToORID(oId)));
        if (oRecord != null) {
            return entityInformation.convertToEntity(oRecord);
        }
        return null;
    }

    public <T, ID> List<T> findAll(final OrientdbEntityInformation<T, ID> entityInformation) {
        List<T> all = new ArrayList<>();
        withSession(session -> {
            for (OElement oRecord : session.browseClass(entityInformation.getEntityName())) {
                all.add(entityInformation.convertToEntity(oRecord));
            }
        });
        return all;
    }

    public <T, ID> List<T> findAll(final String clusterName,
                                   final OrientdbEntityInformation<T, ID> entityInformation) {
        List<T> all = new ArrayList<>();
        withSession(session -> {
            for (ORecord oRecord : session.browseCluster(clusterName)) {
                all.add(entityInformation.convertToEntity(oRecord.getRecord()));
            }
        });
        return all;
    }

    public <T, ID> Long count(final OrientdbEntityInformation<T, ID> entityInformation) {
        return doWithSession(session -> session.countClass(entityInformation.getEntityName()));
    }

    public <T, ID> TypedQuery<T, ID> createQuery(final String query,
                                                 final OrientdbEntityInformation<T, ID> entityInformation) {
        return doWithSession(session ->
                new TypedQuery<>(session.query(format(query, entityInformation.getEntityName())), entityInformation));
    }

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

    private void withSession(final Consumer<ODatabaseSession> consumer) {
        Object sessionHolder = TransactionSynchronizationManager.getResource(sessionFactory);
        if (sessionHolder != null) {
            consumer.accept(((SessionHolder) sessionHolder).getSession());
        } else {
            // If it is not in a transaction, every call use a independent session
            ODatabaseSession session = sessionFactory.openSession();
            consumer.accept(session);
            session.close();
        }
    }
}
