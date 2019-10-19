package org.springframework.data.repository.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.data.repository.orientdb3.repository.EntityProperty;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

import static org.springframework.data.repository.orientdb3.repository.util.Constants.TYPES_BY_CLASS;

public abstract class PropertyHandler<T> {
    private final Field field;
    private final String propertyName;

    public PropertyHandler(final Field field) {
        this.field = field;

        EntityProperty orientdbProperty = field.getAnnotation(EntityProperty.class);
        if (orientdbProperty != null && !StringUtils.isEmpty(orientdbProperty.name())) {
            this.propertyName = orientdbProperty.name();
        } else {
            this.propertyName = field.getName();
        }
    }

    protected Object convertObjectTypeProperty(final Class clazz, final Object value, final ODatabaseSession session,
                                               final OrientdbIdParserHolder parserHolder) {
        OType type = getPropertyDbType(clazz);
        if (type == OType.EMBEDDED) {
            return new OrientdbEntityInformation(clazz, parserHolder).convertToORecord(value, session);
        }
        return value;
    }

    protected Object convertObjectToJavaProperty(final OrientdbIdParserHolder parserHolder,
                                                 final Class clazz, final Object value) {
        if (value instanceof OElement) {
            return new OrientdbEntityInformation(clazz, parserHolder).convertToEntity((OElement) value);
        }
        return value;
    }

    public OType getPropertyDbType() {
        return getPropertyDbType(field.getType());
    }

    private OType getPropertyDbType(final Class clazz) {
        OType oType = OType.getTypeByClass(clazz);
        if (oType == null) {
            oType = TYPES_BY_CLASS.getOrDefault(clazz, OType.EMBEDDED);
        }
        return oType;
    }

    public abstract Object convertProperty(final OElement oElement, final T entity, final ODatabaseSession session);

    public abstract Object convertToJavaProperty(final OElement oElement);

    public Field getPropertyField() {
        return field;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
