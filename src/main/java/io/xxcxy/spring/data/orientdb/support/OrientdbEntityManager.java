package io.xxcxy.spring.data.orientdb.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.ORecord;
import io.xxcxy.spring.data.orientdb.repository.query.TypedQuery;
import io.xxcxy.spring.data.orientdb.repository.support.OrientdbEntityInformation;
import io.xxcxy.spring.data.orientdb.transaction.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

public class OrientdbEntityManager {

    private final ODatabaseSession defaultSession;
    private final SessionFactory sessionFactory;

    public OrientdbEntityManager(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.defaultSession = sessionFactory.openSession();
    }

    public <T, ID> void persist(final T entity, final OrientdbEntityInformation<T, ID> entityInformation) {
        ORecord oRecord = entityInformation.convertToORecord(entity);
        getSession().save(oRecord);
        entityInformation.setId(entity, oRecord.getIdentity());
    }

    public <T, ID> void remove(final T entity, final OrientdbEntityInformation<T, ID> entityInformation) {
        getSession().delete(entityInformation.convertToORecord(entity));
    }

    public <T, ID> T find(final ID oId, final OrientdbEntityInformation<T, ID> entityInformation) {
        OElement oRecord = getSession().load(entityInformation.convertToORID(oId));
        if (oRecord != null) {
            return entityInformation.convertToEntity(oRecord);
        }
        return null;
    }

    public <T, ID> List<T> findAll(final OrientdbEntityInformation<T, ID> entityInformation) {
        List<T> all = new ArrayList<>();
        for (OElement oRecord : getSession().browseClass(entityInformation.getEntityName())) {
            all.add(entityInformation.convertToEntity(oRecord));
        }
        return all;
    }

    public <T, ID> Long count(final OrientdbEntityInformation<T, ID> entityInformation) {
        return getSession().countClass(entityInformation.getEntityName());
    }

    public <T, ID> TypedQuery<T, ID> createQuery(final String query,
                                                 final OrientdbEntityInformation<T, ID> entityInformation) {
        return new TypedQuery<>(getSession().query(String.format(query, entityInformation.getEntityName())),
                entityInformation);
    }

    private ODatabaseSession getSession() {
        Object sessionHolder = TransactionSynchronizationManager.getResource(sessionFactory);
        if (sessionHolder != null) {
            return ((SessionHolder) sessionHolder).getSession();
        }
        return defaultSession;
    }
}
