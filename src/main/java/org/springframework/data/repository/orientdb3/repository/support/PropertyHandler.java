package org.springframework.data.repository.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.data.repository.orientdb3.repository.EntityProperty;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

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
        return new OrientdbEntityInformation(clazz, parserHolder).convertToORecord(value, session);
    }

    protected Object convertObjectToJavaProperty(final OrientdbIdParserHolder parserHolder,
                                                 final Class clazz, final OElement value) {
        return new OrientdbEntityInformation(clazz, parserHolder).convertToEntity(value);
    }

    public OType getPropertyDbType() {
        return OType.getTypeByClass(field.getType());
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
