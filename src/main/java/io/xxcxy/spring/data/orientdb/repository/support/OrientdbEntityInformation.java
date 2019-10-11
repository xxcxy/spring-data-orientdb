package io.xxcxy.spring.data.orientdb.repository.support;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.impl.ODocument;
import io.xxcxy.spring.data.orientdb.repository.OrientdbEntity;
import io.xxcxy.spring.data.orientdb.repository.OrientdbId;
import io.xxcxy.spring.data.orientdb.repository.OrientdbProperty;
import io.xxcxy.spring.data.orientdb.repository.exception.EntityConvertException;
import io.xxcxy.spring.data.orientdb.repository.exception.EntityInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDED;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINK;

public class OrientdbEntityInformation<T, ID> implements EntityInformation<T, ID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientdbEntityInformation.class);

    private final Class<T> domainClass;
    private final OrientdbIdParserHolder parserHolder;
    private final String entityName;
    private final List<PropertyHandler> propertyHandlers;
    private Pair<Field, OrientdbIdParser> idInfo;

    public OrientdbEntityInformation(final Class<T> domainClass, final OrientdbIdParserHolder parserHolder) {
        this.domainClass = domainClass;
        this.entityName = getEntityName(domainClass);
        this.propertyHandlers = new ArrayList<>();
        this.parserHolder = parserHolder;
        ReflectionUtils.doWithFields(domainClass, field -> {
            field.setAccessible(true);
            Annotation[] annotations = field.getAnnotations();
            Optional<OrientdbId> orientdbId = getOrientdbId(annotations);
            if (orientdbId.isPresent()) {
                if (idInfo != null) {
                    throw new EntityInitException("entity cant have two id properties");
                }
                idInfo = Pair.of(field, getIdParser(field, orientdbId.get()));
            } else {
                if (!Modifier.isTransient(field.getModifiers())) {
                    propertyHandlers.add(new PropertyHandler(field, getOrientdbProperty(annotations)));
                }
            }
        });
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

    private Optional<OrientdbId> getOrientdbId(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof OrientdbId) {
                return Optional.of((OrientdbId) annotation);
            }
        }
        return Optional.empty();
    }

    @Nullable
    private OrientdbProperty getOrientdbProperty(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof OrientdbProperty) {
                return (OrientdbProperty) annotation;
            }
        }
        return null;
    }

    private String getEntityName(final Class<T> domainClass) {
        try {
            OrientdbEntity entityAnnotation = domainClass.getAnnotation(OrientdbEntity.class);
            if (!StringUtils.isEmpty(entityAnnotation.name())) {
                return entityAnnotation.name();
            }
        } catch (Exception e) {
            LOGGER.error("entity class[{}] must have OrientdbEntity annotation!", domainClass, e);
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

    public OElement convertToORecord(final T entity) {
        OElement oElement = newOElement(entity);
        for (PropertyHandler propertyHandler : propertyHandlers) {
            oElement.setProperty(propertyHandler.getPropertyName(), propertyHandler.convertProperty(entity),
                    propertyHandler.getPropertyDbType());
        }
        return oElement;
    }

    private OElement newOElement(final T entity) {
        if (idInfo == null || isNew(entity)) {
            return new ODocument(getEntityName());
        } else {
            return new ODocument(getEntityName(), convertToORID(getId(entity)));
        }
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

    private class PropertyHandler {

        private final Field field;
        private final String propertyName;
        private final boolean isEmbedded;

        private PropertyHandler(final Field field, @Nullable final OrientdbProperty orientdbProperty) {
            this.field = field;
            if (orientdbProperty != null) {
                isEmbedded = orientdbProperty.isEmbedded();
                if (!StringUtils.isEmpty(orientdbProperty.name())) {
                    this.propertyName = orientdbProperty.name();
                } else {
                    this.propertyName = field.getName();
                }
            } else {
                isEmbedded = true;
                this.propertyName = field.getName();
            }
        }


        private Object convertProperty(final T entity) {
            Object value = ReflectionUtils.getField(field, entity);
            OType oType = getPropertyDbType();
            if (oType == EMBEDDED || oType == LINK) {
                return new OrientdbEntityInformation(field.getType(), parserHolder).convertToORecord(value);
            }
            return value;
        }

        private String getPropertyName() {
            return propertyName;
        }

        private OType getPropertyDbType() {
            OType oType = OType.getTypeByClass(field.getType());
            if (!isEmbedded) {
                if (oType == EMBEDDED) {
                    return LINK;
                } else {
                    LOGGER.error("{} must be embedded", field.getType());
                }
            }
            return oType;
        }

        private Field getPropertyField() {
            return field;
        }

        private Object convertToJavaProperty(final OElement oElement) {
            OType oType = getPropertyDbType();
            if (oType == EMBEDDED || oType == LINK) {
                return new OrientdbEntityInformation(field.getType(), parserHolder)
                        .convertToEntity(oElement.getProperty(propertyName));
            }
            return OType.convert(oElement.getProperty(propertyName), field.getType());
        }
    }
}
