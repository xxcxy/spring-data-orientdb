package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.data.orientdb3.repository.EntityProperty;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.springframework.data.orientdb3.repository.util.Constants.TYPES_BY_CLASS;

/**
 * Handles entities property.
 *
 * @author xxcxy
 */
public abstract class PropertyHandler {
    private final Field field;
    private final String propertyName;

    /**
     * Creates a {@link PropertyHandler}.
     *
     * @param field
     */
    public PropertyHandler(final Field field) {
        this.field = field;

        EntityProperty orientdbProperty = field.getAnnotation(EntityProperty.class);
        if (orientdbProperty != null && !StringUtils.isEmpty(orientdbProperty.name())) {
            this.propertyName = orientdbProperty.name();
        } else {
            this.propertyName = field.getName();
        }
    }

    /**
     * Converts a java Object to a orientdb type.
     *
     * @param clazz
     * @param value
     * @param session
     * @param parserHolder
     * @param converted
     * @return
     */
    protected Object convertToOrientdbProperty(final Class clazz, final Object value, final ODatabaseSession session,
                                               final OrientdbIdParserHolder parserHolder,
                                               final Map<Object, OElement> converted) {
        if (converted.containsKey(value)) {
            return converted.get(value);
        }
        OType type = getOrientdbType(clazz);
        if (type == OType.EMBEDDED) {
            return new OrientdbEntityInformation(clazz, parserHolder).convertToORecord(value, session, converted);
        }
        return value;
    }

    /**
     * Converts a orientdb type to a java Object.
     *
     * @param parserHolder
     * @param clazz
     * @param value
     * @param converted
     * @return
     */
    protected Object convertToJavaProperty(final OrientdbIdParserHolder parserHolder,
                                           final Class clazz, final Object value,
                                           final Map<OElement, Object> converted) {
        return convertToOElement(value).map(oe -> {
            if (converted.containsKey(oe)) {
                return converted.get(oe);
            } else {
                Object obj = new OrientdbEntityInformation(clazz, parserHolder)
                        .getEntityProxy(oe, converted);
                converted.put(oe, obj);
                return obj;
            }
        }).orElse(value);
    }

    /**
     * Converts orientdb type to a {@link OElement}.
     *
     * @param value
     * @return
     */
    private Optional<OElement> convertToOElement(final Object value) {
        if (value instanceof OElement) {
            return Optional.of((OElement) value);
        } else if (value instanceof ORecordId) {
            return Optional.of(((ORecordId) value).getRecord());
        }
        return Optional.empty();
    }

    /**
     * Gets the field's {@link OType}.
     *
     * @return
     */
    public OType getOrientdbType() {
        return getOrientdbType(field.getType());
    }

    /**
     * Gets corresponding {@link OType} of the orientdb.
     *
     * @param clazz
     * @return
     */
    private OType getOrientdbType(final Class clazz) {
        OType oType = OType.getTypeByClass(clazz);
        if (oType == null) {
            oType = TYPES_BY_CLASS.getOrDefault(clazz, OType.EMBEDDED);
        }
        return oType;
    }

    /**
     * Sets the {@link OElement} property.
     *
     * @param oElement
     * @param value
     * @param session
     * @param converted
     */
    public abstract void setOElementProperty(final OElement oElement, final Object value,
                                             final ODatabaseSession session, final Map<Object, OElement> converted);

    /**
     * Converts a {@link OElement} to a java Object.
     *
     * @param oElement
     * @param converted
     * @return
     */
    public abstract Object getPropertyInJavaType(final OElement oElement, final Map<OElement, Object> converted);

    /**
     * Gets the field
     *
     * @return
     */
    public Field getPropertyField() {
        return field;
    }

    /**
     * Get the propertyName
     *
     * @return
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Transforms the map's value.
     *
     * @param source
     * @param converter
     * @return
     */
    protected Map<String, Object> convertMap(final Map<String, Object> source,
                                             final BiFunction<Class, Object, Object> converter) {
        Map<String, Object> map = new HashMap<>();
        Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            map.put(entry.getKey(), converter.apply(type, entry.getValue()));
        }
        return map;
    }

    /**
     * Transforms the collection's element.
     *
     * @param source
     * @param target
     * @param converter
     * @param <T>
     * @return
     */
    protected <T> Collection<Object> convertCollection(final Iterable<T> source,
                                                       final Collection<Object> target,
                                                       final BiFunction<Class, Object, Object> converter) {
        if (source == null) {
            return target;
        }
        Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        for (T obj : source) {
            target.add(converter.apply(type, obj));
        }
        return target;
    }

    public abstract boolean isCascade();
}
