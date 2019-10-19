package org.springframework.data.repository.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.data.repository.orientdb3.repository.FromVertex;
import org.springframework.data.repository.orientdb3.repository.ToVertex;
import org.springframework.data.repository.orientdb3.repository.exception.EntityConvertException;
import org.springframework.data.repository.orientdb3.repository.exception.EntityInitException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDED;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDMAP;

public class EdgePropertyHandler<T> extends PropertyHandler<T> {

    private final Field field;
    private final OrientdbIdParserHolder parserHolder;
    private final boolean isFrom;
    private final boolean isTo;
    private final OType oType;

    public EdgePropertyHandler(final Field field, final OrientdbIdParserHolder parserHolder) {
        super(field);
        this.field = field;
        this.parserHolder = parserHolder;
        this.oType = getPropertyDbType();

        if (field.getAnnotation(FromVertex.class) != null) {
            isFrom = true;
        } else {
            isFrom = false;
        }

        if (field.getAnnotation(ToVertex.class) != null) {
            isTo = true;
        } else {
            isTo = false;
        }

        if (isFrom && isTo) {
            throw new EntityInitException("A Edge property must not be both fromVertex and toVertex");
        }
    }

    public Object convertProperty(final OElement oElement, final T entity, final ODatabaseSession session) {
        // set fromVertex and toVertex on oElement init
        if (isFrom || isTo) {
            return null;
        }
        Object value = ReflectionUtils.getField(field, entity);
        if (oType == EMBEDDED) {
            return convertObjectTypeProperty(field.getType(), value, session, parserHolder);
        } else if (oType == EMBEDDEDMAP) {
            Map<String, Object> map = new HashMap<>();
            Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                map.put(entry.getKey(), convertObjectTypeProperty(type, entry.getValue(), session, parserHolder));
            }
            return map;
        }
        return value;
    }

    public Object convertToJavaProperty(final OElement oElement) {
        if (isFrom) {
            OEdge oEdge = oElement.asEdge().orElseThrow(() -> new EntityConvertException("Must be a OEdge"));
            return convertObjectToJavaProperty(parserHolder, field.getType(), oEdge.getFrom());
        }
        if (isTo) {
            OEdge oEdge = oElement.asEdge().orElseThrow(() -> new EntityConvertException("Must be a OEdge"));
            return convertObjectToJavaProperty(parserHolder, field.getType(), oEdge.getTo());
        }
        if (oType == EMBEDDED) {
            return convertObjectToJavaProperty(parserHolder, field.getType(), oElement.getProperty(getPropertyName()));
        }
        if (oType == EMBEDDEDMAP) {
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
}
