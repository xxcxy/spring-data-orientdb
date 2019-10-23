package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import org.springframework.data.orientdb3.repository.Edge;
import org.springframework.data.orientdb3.repository.Embedded;
import org.springframework.data.orientdb3.repository.exception.EntityConvertException;
import org.springframework.data.orientdb3.repository.exception.EntityInitException;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDED;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDLIST;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDMAP;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDSET;
import static org.springframework.data.orientdb3.repository.util.Constants.OBJECT_TYPE;

public class VertexPropertyHandler extends PropertyHandler {

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

    // TODO refactor
    @Override
    public void setOElementProperty(final OElement oElement, final Object value, final ODatabaseSession session) {
        String propertyName = getPropertyName();
        oElement.removeProperty(propertyName);
        OVertex oVertex = oElement.asVertex().orElseThrow(() -> new EntityConvertException("Must be a OVertex"));
        if (OBJECT_TYPE.containsKey(oType)) {
            if (isEdge) {
                if (oType == EMBEDDED) {
                    for (OEdge old : oVertex.getEdges(ODirection.OUT, getEdgeName())) {
                        old.delete();
                    }
                    if (value != null) {
                        oVertex.addEdge((OVertex) convertObjectTypeProperty(field.getType(),
                                value, session, parserHolder), getEdgeName());
                    }
                } else if (oType == EMBEDDEDLIST || oType == EMBEDDEDSET) {
                    Class gType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    List<OVertex> newEdge = new ArrayList<>();
                    if (value != null) {
                        for (Object obj : (Collection) value) {
                            newEdge.add((OVertex) convertObjectTypeProperty(gType, obj, session, parserHolder));
                        }
                    }
                    for (OEdge old : oVertex.getEdges(ODirection.OUT, getEdgeName())) {
                        if (newEdge.contains(old.getTo())) {
                            newEdge.remove(old.getTo());
                        } else {
                            old.delete();
                        }
                    }
                    for (OVertex to : newEdge) {
                        oVertex.addEdge(to, getEdgeName());
                    }
                } else {
                    throw new EntityConvertException(oType.name() + " type is not support for edge");
                }
            } else if (value == null) {
                return;
            } else if (oType == EMBEDDED) {
                oVertex.setProperty(propertyName,
                        convertObjectTypeProperty(field.getType(), value, session, parserHolder), oType);
            } else if (oType == EMBEDDEDMAP) {
                Map<String, Object> map = new HashMap<>();
                Class type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                    map.put(entry.getKey(), convertObjectTypeProperty(type, entry.getValue(), session, parserHolder));
                }
                oVertex.setProperty(propertyName, map, oType);
            } else {
                oVertex.setProperty(propertyName, value, oType);
            }
        } else if (value != null) {
            oVertex.setProperty(propertyName, value, oType);
        }
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
            } else if (oType == EMBEDDEDLIST) {
                List list = new ArrayList();
                Class gType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                for (OEdge oEdge : edges) {
                    list.add(convertObjectToJavaProperty(parserHolder, gType, oEdge.getTo()));
                }
                return list;
            } else if (oType == EMBEDDEDSET) {
                Set set = new HashSet();
                Class gType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                for (OEdge oEdge : edges) {
                    set.add(convertObjectToJavaProperty(parserHolder, gType, oEdge.getTo()));
                }
                return set;
            }
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
