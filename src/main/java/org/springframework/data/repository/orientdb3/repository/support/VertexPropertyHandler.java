package org.springframework.data.repository.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import org.springframework.data.repository.orientdb3.repository.Edge;
import org.springframework.data.repository.orientdb3.repository.Embedded;
import org.springframework.data.repository.orientdb3.repository.exception.EntityConvertException;
import org.springframework.data.repository.orientdb3.repository.exception.EntityInitException;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDED;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDLIST;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDMAP;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDSET;
import static org.springframework.data.repository.orientdb3.repository.util.Constants.OBJECT_TYPE;

public class VertexPropertyHandler<T> extends PropertyHandler<T> {

    private final Field field;
    private final OrientdbIdParserHolder parserHolder;
    private final boolean isEdge;
    private final boolean isEmbedded;
    private final OType oType;

    public VertexPropertyHandler(final Field field, final OrientdbIdParserHolder parserHolder) {
        super(field);
        this.field = field;
        this.parserHolder = parserHolder;
        this.oType = getPropertyDbType();


        if (field.getAnnotation(Embedded.class) != null) {
            isEmbedded = true;
        } else {
            isEmbedded = false;
        }

        if (field.getAnnotation(Edge.class) != null) {
            isEdge = true;
        } else {
            isEdge = false;
        }

        if (isEdge && isEmbedded) {
            throw new EntityInitException("A Entity property must not be both embedded and edge");
        }
    }

    public Object convertProperty(final OElement oElement, final T entity, final ODatabaseSession session) {
        Object value = ReflectionUtils.getField(field, entity);
        OVertex oVertex = oElement.asVertex().orElseThrow(() -> new EntityConvertException("Must be a OVertex"));

        if (OBJECT_TYPE.containsKey(oType)) {
            if (isEdge) {
                if (oType == EMBEDDED) {
                    oVertex.addEdge((OVertex) convertObjectTypeProperty(field.getType(), value,
                            session, parserHolder), getEdgeName());
                } else if (oType == EMBEDDEDLIST || oType == EMBEDDEDSET) {
                    for (Object obj : (Collection) value) {
                        oVertex.addEdge((OVertex) convertObjectTypeProperty(field.getType(), obj,
                                session, parserHolder), getEdgeName());
                    }
                } else {
                    throw new EntityConvertException(oType.name() + " type is not support for edge");
                }
                return null;
            } else if (oType == EMBEDDED) {
                return convertObjectTypeProperty(field.getType(), value, session, parserHolder);
            } else if (oType == EMBEDDEDMAP) {
                Map<String, Object> map = new HashMap<>();
                Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                    map.put(entry.getKey(), convertObjectTypeProperty(type, entry.getValue(), session, parserHolder));
                }
                return map;
            }
        }
        return value;
    }

    private String getEdgeName() {
        Edge edge = field.getAnnotation(Edge.class);
        String edgeName = edge.name();
        if (StringUtils.isEmpty(edgeName)) {
            edgeName = StringUtils.capitalize(field.getName());
        }
        return edgeName;
    }

    public Object convertToJavaProperty(final OElement oElement) {
        OVertex oVertex = oElement.asVertex().orElseThrow(() -> new EntityConvertException("Must be a OVertex"));
        if (isEdge) {
            Iterable<OEdge> edges = oVertex.getEdges(ODirection.OUT, getEdgeName());
            if (oType == EMBEDDED) {
                for (OEdge oEdge : edges) {
                    return convertObjectToJavaProperty(parserHolder, field.getType(), oEdge.getTo());
                }
            } else if (oType == EMBEDDEDLIST || oType == EMBEDDEDSET) {
                try {
                    Collection collection = (Collection) field.getType().newInstance();
                    Class gType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    for (OEdge oEdge : edges) {
                        collection.add(convertObjectToJavaProperty(parserHolder, gType, oEdge.getTo()));
                    }
                    return collection;
                } catch (Exception e) {
                    throw new EntityConvertException("Collection property must have a no-args constructor");
                }
            }
        }
        if (oType == EMBEDDED) {
            return convertObjectToJavaProperty(parserHolder, field.getType(), oElement.getProperty(getPropertyName()));
        }
        if (oType == EMBEDDEDMAP) {
            Map<String, Object> map = new HashMap<>();
            Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
            Map<String, OElement> recordMap = oElement.getProperty(getPropertyName());
            for (Map.Entry<String, OElement> entry : recordMap.entrySet()) {
                map.put(entry.getKey(), convertObjectToJavaProperty(parserHolder, type, entry.getValue()));
            }
            return map;
        }
        return OType.convert(oElement.getProperty(getPropertyName()), field.getType());
    }
}
