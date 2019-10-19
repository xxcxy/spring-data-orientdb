package org.springframework.data.repository.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.data.repository.orientdb3.repository.Embedded;
import org.springframework.data.repository.orientdb3.repository.Link;
import org.springframework.data.repository.orientdb3.repository.exception.EntityConvertException;
import org.springframework.data.repository.orientdb3.repository.exception.EntityInitException;
import org.springframework.util.ReflectionUtils;

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

import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDED;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDMAP;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINK;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINKLIST;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINKMAP;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINKSET;
import static org.springframework.data.repository.orientdb3.repository.util.Constants.OBJECT_TYPE;

public class ElementPropertyHandler<T> extends PropertyHandler<T> {
    private final Field field;
    private final OrientdbIdParserHolder parserHolder;
    private final boolean isLink;
    private final boolean isEmbedded;
    private final OType oType;

    ElementPropertyHandler(final Field field, final OrientdbIdParserHolder parserHolder) {
        super(field);
        this.field = field;
        this.parserHolder = parserHolder;
        if (field.getAnnotation(Embedded.class) != null) {
            isEmbedded = true;
        } else {
            isEmbedded = false;
        }

        if (field.getAnnotation(Link.class) != null) {
            isLink = true;
        } else {
            isLink = false;
        }

        if (isLink && isEmbedded) {
            throw new EntityInitException("A Entity property must not be both embedded and link");
        }

        // After set isEmbedded or isLink
        this.oType = getPropertyDbType();
    }


    public Object convertProperty(final OElement oElement, final T entity, final ODatabaseSession session) {
        Object value = ReflectionUtils.getField(field, entity);
        if (oType == EMBEDDED || oType == LINK) {
            return convertObjectTypeProperty(field.getType(), value, session, parserHolder);
        }
        if (oType == LINKLIST) {
            List list = new ArrayList();
            mapToRecord((Collection) value, session, o -> list.add(o));
            return list;
        }
        if (oType == LINKSET) {
            Set set = new HashSet();
            mapToRecord((Collection) value, session, o -> set.add(o));
            return set;
        }
        if (oType == LINKMAP || oType == EMBEDDEDMAP) {
            Map<String, Object> map = new HashMap<>();
            Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                map.put(entry.getKey(), convertObjectTypeProperty(type, entry.getValue(), session, parserHolder));
            }
            return map;
        }
        return value;
    }

    private void mapToRecord(final Collection collection, final ODatabaseSession session, final Consumer consumer) {
        Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        for (Object ele : collection) {
            consumer.accept(convertObjectTypeProperty(type, ele, session, parserHolder));
        }
    }

    // Change the field java type to OType using OType.getTypeByClass
    public OType getPropertyDbType() {
        OType oType = super.getPropertyDbType();
        if (OBJECT_TYPE.containsKey(oType)) {
            if (isEmbedded == true && isLink == false) {
                return oType;
            } else if (isEmbedded == false && isLink == true) {
                return OBJECT_TYPE.get(oType);
            } else {
                throw new EntityConvertException("Object property must be a embedded or a link annotation!");
            }
        }
        return oType;
    }

    public Object convertToJavaProperty(final OElement oElement) {
        if (oType == EMBEDDED || oType == LINK) {
            return convertObjectToJavaProperty(parserHolder, field.getType(), oElement.getProperty(getPropertyName()));
        }
        // Every element has it's own converter so can't use OType.convert
        if (oType == LINKLIST) {
            List list = new ArrayList();
            mapToObject(oElement.getProperty(getPropertyName()), o -> list.add(o));
            return list;
        }
        if (oType == LINKSET) {
            Set set = new HashSet();
            mapToObject(oElement.getProperty(getPropertyName()), o -> set.add(o));
            return set;
        }
        if (oType == LINKMAP || oType == EMBEDDEDMAP) {
            Map<String, Object> map = new HashMap<>();
            Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
            Map<String, Object> recordMap = oElement.getProperty(getPropertyName());
            for (Map.Entry<String, Object> entry : recordMap.entrySet()) {
                map.put(entry.getKey(), convertObjectToJavaProperty(parserHolder, type, entry.getValue()));
            }
            return map;
        }
        return OType.convert(oElement.getProperty(getPropertyName()), field.getType());
    }

    private void mapToObject(final Collection<OElement> collection, final Consumer consumer) {
        Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        for (OElement ele : collection) {
            consumer.accept(convertObjectToJavaProperty(parserHolder, type, ele));
        }
    }
}
