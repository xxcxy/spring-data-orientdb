package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import org.springframework.data.orientdb3.repository.EdgeEntity;
import org.springframework.data.orientdb3.repository.ElementEntity;
import org.springframework.data.orientdb3.repository.FromVertex;
import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.repository.ToVertex;
import org.springframework.data.orientdb3.repository.VertexEntity;
import org.springframework.data.orientdb3.repository.exception.EntityConvertException;
import org.springframework.data.orientdb3.repository.exception.EntityInitException;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.util.Pair;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class OrientdbEntityInformation<T, ID> implements EntityInformation<T, ID> {

    private final Class<T> domainClass;
    private final EntityType entityType;
    private final OrientdbIdParserHolder parserHolder;
    private final String entityName;
    private final List<PropertyHandler> propertyHandlers;
    private Pair<Field, OrientdbIdParser> idInfo;
    private Field fromField;
    private Field toField;

    public OrientdbEntityInformation(final Class<T> domainClass, final OrientdbIdParserHolder parserHolder) {
        this.domainClass = domainClass;
        this.entityName = getEntityName(domainClass);
        this.propertyHandlers = new ArrayList<>();
        this.parserHolder = parserHolder;
        this.entityType = EntityType.getEntityType(domainClass).orElseThrow(() ->
                new EntityInitException("Entity class must have one of the annotation(ElementEntity," +
                        " VertexEntity, EdgeEntity)"));

        // Get a propertyHandler according to the class annotation
        Function<Field, PropertyHandler<T>> handlerGenerator = getHandlerGenerator(parserHolder);

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
                    propertyHandlers.add(handlerGenerator.apply(field));
                }
            }
        });
    }

    private void setFromToField(final Field field) {
        if (field.getAnnotation(FromVertex.class) != null) {
            this.fromField = field;
        } else if (field.getAnnotation(ToVertex.class) != null) {
            this.toField = field;
        }
    }

    private Function<Field, PropertyHandler<T>> getHandlerGenerator(final OrientdbIdParserHolder parserHolder) {
        switch (entityType) {
            case ELEMENT:
                return field -> new ElementPropertyHandler<>(field, parserHolder);
            case VERTEX:
                return field -> new VertexPropertyHandler<>(field, parserHolder);
            case EDGE:
                return field -> new EdgePropertyHandler<>(field, parserHolder);
            default:
                throw new EntityInitException("can't be here");
        }
    }

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

    private String getEntityName(final Class<T> domainClass) {
        if (entityType == EntityType.ELEMENT) {
            String name = domainClass.getAnnotation(ElementEntity.class).name();
            if (!StringUtils.isEmpty(name)) {
                return name;
            }
        }
        if (entityType == EntityType.VERTEX) {
            String name = domainClass.getAnnotation(VertexEntity.class).name();
            if (!StringUtils.isEmpty(name)) {
                return name;
            }
        }
        if (entityType == EntityType.EDGE) {
            String name = domainClass.getAnnotation(EdgeEntity.class).name();
            if (!StringUtils.isEmpty(name)) {
                return name;
            }
        }
        return domainClass.getSimpleName();
    }

    @Override
    public boolean isNew(final T t) {
        return ReflectionUtils.getField(idInfo.getFirst(), t) == null;
    }

    @Override
    public ID getId(final T t) {
        return (ID) ReflectionUtils.getField(idInfo.getFirst(), t);
    }

    @Override
    public Class<ID> getIdType() {
        return (Class<ID>) idInfo.getFirst().getType();
    }

    @Override
    public Class<T> getJavaType() {
        return domainClass;
    }

    public OElement convertToORecord(final T entity, final ODatabaseSession session) {
        OElement oElement = newOElement(entity, session);
        for (PropertyHandler propertyHandler : propertyHandlers) {
            Object obj = propertyHandler.convertProperty(oElement, entity, session);

            // the obj is null when the property is edge or fromVertex or toVertex
            if (obj != null) {
                oElement.setProperty(propertyHandler.getPropertyName(),
                        propertyHandler.convertProperty(oElement, entity, session),
                        propertyHandler.getPropertyDbType());
            }
        }
        return oElement;
    }

    private OElement newOElement(final T entity, final ODatabaseSession session) {
        if (isNew(entity)) {
            switch (entityType) {
                case ELEMENT:
                    return session.newElement(getEntityName());
                case VERTEX:
                    return session.newVertex(getEntityName());
                case EDGE:
                    return session.newEdge(getFromVertex(entity, session), getToVertex(entity, session),
                            getEntityName());
                default:
                    throw new EntityConvertException("can't be here");
            }
        } else {
            return session.load(convertToORID(getId(entity)));
        }
    }

    private OVertex getFromVertex(final T entity, final ODatabaseSession session) {
        if (fromField == null) {
            throw new EntityConvertException("EdgeEntity must have fromFiled.");
        }
        return (OVertex) new OrientdbEntityInformation(fromField.getType(), parserHolder)
                .convertToORecord(ReflectionUtils.getField(fromField, entity), session);
    }

    private OVertex getToVertex(final T entity, final ODatabaseSession session) {
        if (toField == null) {
            throw new EntityConvertException("EdgeEntity must have toField.");
        }
        return (OVertex) new OrientdbEntityInformation(toField.getType(), parserHolder)
                .convertToORecord(ReflectionUtils.getField(toField, entity), session);
    }

    public ORID convertToORID(final ID id) {
        return idInfo.getSecond().parseJavaId(id);
    }

    public void setId(final T entity, final ORID orid) {
        Object id = idInfo.getSecond().parseOrientdbId(orid);
        ReflectionUtils.setField(idInfo.getFirst(), entity, id);
    }

    public T convertToEntity(final OElement oRecord) {
        try {
            T entity = domainClass.newInstance();
            if (idInfo != null) {
                setId(entity, oRecord.getIdentity());
            }
            for (PropertyHandler propertyHandler : propertyHandlers) {
                ReflectionUtils.setField(propertyHandler.getPropertyField(), entity,
                        propertyHandler.convertToJavaProperty(oRecord));
            }
            return entity;
        } catch (Exception e) {
            throw new EntityConvertException("orientdb entity must have no-argument constructor");
        }
    }

    public String getEntityName() {
        return entityName;
    }

}
