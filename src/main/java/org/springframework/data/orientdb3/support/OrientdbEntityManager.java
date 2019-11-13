package org.springframework.data.orientdb3.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.orientdb3.repository.QueryResult;
import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;
import org.springframework.data.orientdb3.transaction.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.springframework.util.ReflectionUtils.doWithFields;
import static org.springframework.util.ReflectionUtils.setField;

/**
 * A orientdb entity manager.
 *
 * @author xxcxy
 */
public class OrientdbEntityManager {

    private static final Logger SQL_LOG = LoggerFactory.getLogger("orientdb.query.sql");
    private static final Logger LOG = LoggerFactory.getLogger(OrientdbEntityManager.class);

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
     * Executes a query.
     *
     * @param query
     * @param entityInformation
     * @param <T>
     * @return
     */
    public <T> List<T> doQuery(final String query, final Object[] parameters,
                               final OrientdbEntityInformation<T, ?> entityInformation) {
        showSql(query, parameters);
        HashMap<OElement, Object> converted = new HashMap<>();
        return doWithSession(session ->
                session.query(query, parameters)
                        .elementStream().map(e -> entityInformation.getEntityProxy(e, converted))
                        .collect(Collectors.toList()));
    }

    /**
     * Executes a query sql.
     *
     * @param query
     * @param parameters
     * @param type
     * @param <T>
     * @return
     */
    public <T> List<T> doQuery(final String query, final Object[] parameters, final Class<T> type) {
        showSql(query, parameters);
        return doWithSession(session ->
                session.query(query, parameters).stream().map(oResult -> convert(oResult, type, true))
                        .collect(Collectors.toList()));
    }

    /**
     * Converts a {@link OResult} to a java type object.
     *
     * @param oResult
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> T convert(final OResult oResult, final Class<T> clazz, final boolean checkAnnotation) {
        if (checkAnnotation && clazz.getAnnotation(QueryResult.class) == null) {
            LOG.error("Projection class must have a QueryResult annotation!");
            return null;
        }
        try {
            T dto = clazz.newInstance();
            fillObject(dto, clazz, name -> oResult.getProperty(name));
            return dto;
        } catch (Exception e) {
            LOG.error("Create new dto error: ", e);
        }
        return null;
    }

    /**
     * Converts a {@link OElement} to a java type object.
     *
     * @param oElement
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> T convert(final OElement oElement, final Class<T> clazz) {
        try {
            T dto = clazz.newInstance();
            fillObject(dto, clazz, name -> oElement.getProperty(name));
            return dto;
        } catch (Exception e) {
            LOG.error("Create new dto error: ", e);
        }
        return null;
    }

    /**
     * Sets a java object's field value.
     *
     * @param t
     * @param clazz
     * @param function
     * @param <T>
     */
    private <T> void fillObject(final T t, final Class<T> clazz, final Function<String, Object> function) {
        doWithFields(clazz, field -> {
            field.setAccessible(true);
            Class fieldType = field.getType();
            Object obj = function.apply(field.getName());
            if (List.class.isAssignableFrom(fieldType)) {
                setField(field, t, convertCollection(new ArrayList(), (Iterable) obj, field));
            } else if (Set.class.isAssignableFrom(fieldType)) {
                setField(field, t, convertCollection(new HashSet(), (Iterable) obj, field));
            } else if (Map.class.isAssignableFrom(fieldType)) {
                Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
                Map map = new HashMap<String, Object>();
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
                    map.put(entry.getKey(), convertToJavaType(entry.getValue(), type));
                }
                setField(field, t, map);
            } else {
                setField(field, t, convertToJavaType(obj, fieldType));
            }
        });
    }

    /**
     * Converts a orientdb's iterable to a java collection.
     *
     * @param collection
     * @param iterable
     * @param field
     * @return
     */
    private Object convertCollection(final Collection collection, final Iterable iterable, final Field field) {
        Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        for (Object oc : iterable) {
            collection.add(convertToJavaType(oc, type));
        }
        return collection;
    }

    /**
     * Converts a orientdb object to a java object.
     *
     * @param orientObj
     * @param javaType
     * @return
     */
    private Object convertToJavaType(final Object orientObj, final Class javaType) {
        if (orientObj == null) {
            return null;
        } else if (OType.isSimpleType(orientObj)) {
            return OType.convert(orientObj, javaType);
        } else if (orientObj instanceof OElement) {
            return convert((OElement) orientObj, javaType);
        }
        return convert((OResult) orientObj, javaType, false);
    }

    /**
     * Executes a command.
     *
     * @param sql
     * @param parameters
     */
    public void doCommand(final String sql, final Object[] parameters) {
        showSql(sql, parameters);
        withSession(session ->
                session.command(sql, parameters));
    }

    /**
     * Executes a count sql.
     *
     * @param sql
     * @param parameters
     * @return
     */
    public Long doQueryCount(final String sql, final Object[] parameters) {
        showSql(sql, parameters);
        return doWithSession(session -> session.query(sql, parameters).next().getProperty("count"));
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
            try {
                R r = function.apply(session);
                return r;
            } finally {
                session.close();
            }
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
            try{
                consumer.accept(session);
            }finally {
                session.close();
            }
        }
    }

    /**
     * Logs the query sql.
     *
     * @param sql
     * @param parameters
     */
    private void showSql(final String sql, final Object[] parameters) {
        SQL_LOG.debug(sql.concat(" {}"), asList(parameters));
    }
}
