package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import org.springframework.data.orientdb3.repository.FromVertex;
import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.repository.ToVertex;
import org.springframework.data.orientdb3.repository.exception.EntityConvertException;
import org.springframework.data.orientdb3.repository.exception.EntityInitException;
import org.springframework.data.orientdb3.support.EntityProxy;
import org.springframework.data.orientdb3.support.EntityProxyInterface;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.ReflectionUtils.setField;

/**
 * Implementation of {@link org.springframework.data.repository.core.EntityInformation}.
 *
 * @author xxcxy
 */
public class OrientdbEntityInformation<T, ID> implements EntityInformation<T, ID> {

    private final Class<T> domainClass;
    private final EntityType entityType;
    private final OrientdbIdParserHolder parserHolder;
    private final String entityName;
    private final Map<String, PropertyHandler> propertyHandlers;
    private Pair<Field, OrientdbIdParser<ID>> idInfo;
    private Field fromField;
    private Field toField;

    /**
     * Creates a new {@link OrientdbEntityInformation} for the given domain class and {@link OrientdbIdParserHolder}.
     *
     * @param domainClass
     * @param parserHolder
     */
    public OrientdbEntityInformation(final Class<T> domainClass, final OrientdbIdParserHolder parserHolder) {
        this.domainClass = domainClass;
        this.propertyHandlers = new HashMap<>();
        this.parserHolder = parserHolder;
        this.entityType = EntityType.getEntityType(domainClass).orElseThrow(() ->
                new EntityInitException(domainClass.getName() + " class must have one of the " +
                        "annotation(ElementEntity, EmbeddedEntity, VertexEntity, EdgeEntity)"));
        this.entityName = getEntityName(domainClass);

        // Get a propertyHandler according to the class annotation
        Function<Field, PropertyHandler> handlerGenerator = getHandlerGenerator(parserHolder);

        // doWithFields will get All fields including fields of superClass
        ReflectionUtils.doWithFields(domainClass, field -> {
            field.setAccessible(true);
            setFromToField(field);

            // Set idInfo if this field has OrientdbId annotation or set a propertyHandler
            OrientdbId orientdbId = field.getAnnotation(OrientdbId.class);
            if (orientdbId != null) {
                if (idInfo != null) {
                    throw new EntityInitException("entity can't have two id properties");
                }
                idInfo = Pair.of(field, getIdParser(field, orientdbId));
            } else {
                if (!Modifier.isTransient(field.getModifiers())) {
                    propertyHandlers.put(field.getName(), handlerGenerator.apply(field));
                }
            }
        });
    }

    /**
     * Assigns the from and to field.
     *
     * @param field
     */
    private void setFromToField(final Field field) {
        if (field.getAnnotation(FromVertex.class) != null) {
            this.fromField = field;
        } else if (field.getAnnotation(ToVertex.class) != null) {
            this.toField = field;
        }
    }

    /**
     * Gets a handlerGenerator for the entityType.
     *
     * @param parserHolder
     * @return
     */
    private Function<Field, PropertyHandler> getHandlerGenerator(final OrientdbIdParserHolder parserHolder) {
        switch (entityType) {
            case ELEMENT:
                return field -> new ElementPropertyHandler(field, parserHolder);
            case VERTEX:
                return field -> new VertexPropertyHandler(field, parserHolder);
            case EDGE:
                return field -> new EdgePropertyHandler(field, parserHolder);
            case EMBEDDED:
                return field -> new ElementPropertyHandler(field, parserHolder);
            default:
                throw new EntityInitException("can't be here");
        }
    }

    /**
     * Gets a {@link OrientdbIdParserHolder} for the field.
     *
     * @param field
     * @param orientdbId
     * @return
     */
    private OrientdbIdParser getIdParser(final Field field, final OrientdbId orientdbId) {
        if (orientdbId.parseBy().length == 0) {
            return parserHolder.getIdParser(field.getType())
                    .orElseThrow(() -> new EntityInitException(String.format("cant find a idParser for %s",
                            field.getType().getSimpleName())));
        }
        return parserHolder.getIdParserByParserClass(orientdbId.parseBy()[0])
                .orElseThrow(() -> new EntityInitException(String.format("cant find a %s idParser",
                        orientdbId.parseBy()[0].getSimpleName())));
    }

    /**
     * Gets the orientdb's class name corresponding to this java class.
     *
     * @param domainClass
     * @return
     */
    private String getEntityName(final Class<T> domainClass) {
        return entityType.getEntityName(domainClass);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.support.AbstractEntityInformation#isNew(java.lang.Object)
     */
    @Override
    public boolean isNew(final T t) {
        return !(t instanceof EntityProxyInterface) && (idInfo == null || getField(idInfo.getFirst(), t) == null);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.EntityInformation#getId(java.lang.Object)
     */
    @Override
    public ID getId(final T t) {
        return (ID) getField(idInfo.getFirst(), t);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.EntityInformation#getIdType()
     */
    @Override
    public Class<ID> getIdType() {
        return (Class<ID>) idInfo.getFirst().getType();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.EntityInformation#getJavaType()
     */
    @Override
    public Class<T> getJavaType() {
        return domainClass;
    }

    /**
     * Converts a java entity to a orientdb oElement.
     *
     * @param entity
     * @param session
     * @param converted
     * @return
     */
    public OElement convertToORecord(final T entity, final ODatabaseSession session,
                                     final Map<Object, OElement> converted) {
        if (converted.containsKey(entity)) {
            return converted.get(entity);
        }
        if (entity instanceof EntityProxyInterface) {
            OElement oElement = ((EntityProxyInterface) entity).findOElement();
            converted.put(entity, oElement);
            return oElement;
        } else {
            OElement oElement = newOElement(entity, session, converted);
            converted.put(entity, oElement);
            for (PropertyHandler propertyHandler : propertyHandlers.values()) {
                propertyHandler.setOElementProperty(oElement,
                        getField(propertyHandler.getPropertyField(), entity), session, converted);
            }
            return oElement;
        }
    }

    /**
     * Saves a java object to orientdb.
     *
     * @param entity
     * @param session
     * @param cluster
     * @param converted
     * @return
     */
    public T save(final T entity, final ODatabaseSession session, @Nullable final String cluster,
                  final Map<Object, OElement> converted) {
        OElement oElement = getElement(entity, session, converted);
        converted.put(entity, oElement);
        for (PropertyHandler propertyHandler : propertyHandlers.values()) {
            propertyHandler.setOElementProperty(oElement,
                    getField(propertyHandler.getPropertyField(), entity), session, converted);
        }
        if (cluster != null) {
            session.save(oElement, cluster);
        } else {
            session.save(oElement);
        }
        setId(entity, oElement);
        return (T) new EntityProxy(entity, oElement, this, new HashMap<>()).getProxyInstance();
    }

    /**
     * Gets a {@link OElement} for a given object.
     *
     * @param entity
     * @param session
     * @param converted
     * @return
     */
    public OElement getElement(final T entity, final ODatabaseSession session, final Map<Object, OElement> converted) {
        if (isNew(entity)) {
            return newOElement(entity, session, converted);
        } else {
            if (entity instanceof EntityProxyInterface) {
                return ((EntityProxyInterface) entity).findOElement();
            }
            return session.load(getORID(entity));
        }
    }

    /**
     * Creates a {@link OElement} for a given object.
     *
     * @param entity
     * @param session
     * @param converted
     * @return
     */
    private OElement newOElement(final T entity, final ODatabaseSession session,
                                 final Map<Object, OElement> converted) {
        switch (entityType) {
            case ELEMENT:
                return session.newElement(getEntityName());
            case VERTEX:
                return session.newVertex(getEntityName());
            case EDGE:
                return session.newEdge(getFromVertex(entity, session, converted),
                        getToVertex(entity, session, converted), getEntityName());
            case EMBEDDED:
                return session.newElement(getEntityName());
            default:
                throw new EntityConvertException("can't be here");
        }
    }

    /**
     * Gets a entityProxy for a given {@link OElement}.
     *
     * @param oElement
     * @param converted
     * @return
     */
    public T getEntityProxy(final OElement oElement, final Map<OElement, Object> converted) {
        try {
            return new EntityProxy<>(getJavaType().newInstance(), oElement, this, converted)
                    .getProxyInstance();
        } catch (Exception e) {
            throw new EntityConvertException("orientdb entity must have no-argument constructor");
        }
    }

    /**
     * Gets from vertex.
     *
     * @param entity
     * @param session
     * @param converted
     * @return
     */
    private OVertex getFromVertex(final T entity, final ODatabaseSession session,
                                  final Map<Object, OElement> converted) {
        if (fromField == null) {
            throw new EntityConvertException("EdgeEntity must have fromFiled.");
        }
        return (OVertex) new OrientdbEntityInformation(fromField.getType(), parserHolder)
                .convertToORecord(getField(fromField, entity), session, converted);
    }

    /**
     * Gets to vertex.
     *
     * @param entity
     * @param session
     * @param converted
     * @return
     */
    private OVertex getToVertex(final T entity, final ODatabaseSession session,
                                final Map<Object, OElement> converted) {
        if (toField == null) {
            throw new EntityConvertException("EdgeEntity must have toField.");
        }
        return (OVertex) new OrientdbEntityInformation(toField.getType(), parserHolder)
                .convertToORecord(getField(toField, entity), session, converted);
    }

    /**
     * Converts java id to {@link ORID}.
     *
     * @param id
     * @return
     */
    public ORID convertToORID(final ID id) {
        return idInfo.getSecond().parseJavaId(id);
    }

    /**
     * Gets a {@link ORID} from a given object.
     *
     * @param t
     * @return
     */
    private ORID getORID(final T t) {
        return convertToORID((ID) getField(idInfo.getFirst(), t));
    }

    /**
     * Gets the entityName.
     *
     * @return
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Determines if this field exists
     *
     * @param fieldName
     * @return
     */
    public boolean hasFieldName(final String fieldName) {
        return propertyHandlers.containsKey(fieldName);
    }

    /**
     * Determines if this field is a id field.
     *
     * @param fieldName
     * @return
     */
    public boolean isId(final String fieldName) {
        return idInfo != null && idInfo.getFirst().getName().equals(fieldName);
    }

    /**
     * Gets the {@link EntityType}.
     *
     * @return
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Gets a java id from the given {@link OElement}.
     *
     * @param oElement
     * @return
     */
    public ID getId(final OElement oElement) {
        return idInfo.getSecond().parseOrientdbId(oElement.getIdentity());
    }

    /**
     * Gets the {@link PropertyHandler} for the given fieldName.
     *
     * @param fieldName
     * @return
     */
    public PropertyHandler getPropertyHandler(final String fieldName) {
        return propertyHandlers.get(fieldName);
    }

    /**
     * Sets the object's Id.
     *
     * @param t
     * @param oElement
     */
    public void setId(final T t, final OElement oElement) {
        if (idInfo != null) {
            setField(idInfo.getFirst(), t, getId(oElement));
        }
    }

    /**
     * Gets all {@link PropertyHandler}
     *
     * @return
     */
    public List<PropertyHandler> getAllPropertyHandlers() {
        return propertyHandlers.values().stream().collect(Collectors.toList());
    }
}
