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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDED;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDLIST;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDMAP;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDSET;
import static org.springframework.data.orientdb3.repository.util.Constants.OBJECT_TYPE;

/**
 * Extension of {@link PropertyHandler} that handle Vertex's property.
 *
 * @author xxcxy
 */
public class VertexPropertyHandler extends PropertyHandler {

    private final Field field;
    private final OrientdbIdParserHolder parserHolder;
    private final boolean isEdge;
    private final boolean isEmbedded;
    private final boolean isCascade;
    private final OType oType;

    /**
     * Creates a new {@link VertexPropertyHandler}.
     *
     * @param field
     * @param parserHolder
     */
    public VertexPropertyHandler(final Field field, final OrientdbIdParserHolder parserHolder) {
        super(field);
        this.field = field;
        this.parserHolder = parserHolder;
        this.oType = getOrientdbType();

        if (field.getAnnotation(Embedded.class) != null) {
            isEmbedded = true;
        } else {
            isEmbedded = false;
        }

        if (field.getAnnotation(Edge.class) != null) {
            isEdge = true;
            isCascade = field.getAnnotation(Edge.class).cascade();
        } else {
            isEdge = false;
            isCascade = false;
        }

        if (isEdge && isEmbedded) {
            throw new EntityInitException("A Entity property must not be both embedded and edge");
        }
    }

    /*
     * (non-Javadoc)
     * @see PropertyHandler#setOElementProperty
     */
    @Override
    public void setOElementProperty(final OElement oElement, final Object value, final ODatabaseSession session,
                                    final Map<Object, OElement> converted) {
        String propertyName = getPropertyName();
        oElement.removeProperty(propertyName);
        OVertex oVertex = oElement.asVertex().orElseThrow(() -> new EntityConvertException("Must be a OVertex"));
        if (OBJECT_TYPE.containsKey(oType)) {
            if (isEdge) {
                setEdgeProperty(oVertex, value, session, converted);
            } else if (value == null) {
                return;
            } else if (oType == EMBEDDED) {
                oVertex.setProperty(propertyName,
                        convertToOrientdbProperty(field.getType(), value, session, parserHolder, converted), oType);
            } else if (oType == EMBEDDEDMAP) {
                Map<String, Object> map = convertMap((Map) value,
                        (type, obj) -> convertToOrientdbProperty(type, obj, session, parserHolder, converted));
                oVertex.setProperty(propertyName, map, oType);
            } else {
                oVertex.setProperty(propertyName, value, oType);
            }
        } else if (value != null) {
            oVertex.setProperty(propertyName, value, oType);
        }
    }

    /**
     * Add a edge to the vertex.
     *
     * @param oVertex
     * @param value
     * @param session
     * @param converted
     */
    private void setEdgeProperty(final OVertex oVertex, final Object value, final ODatabaseSession session,
                                 final Map<Object, OElement> converted) {
        String edgeName = getEdgeName();
        ODirection edgeDirection = getEdgeDirection();
        if (oType == EMBEDDED) {
            for (OEdge old : oVertex.getEdges(edgeDirection, edgeName)) {
                old.delete();
            }
            if (value != null) {
                OVertex diaNode = (OVertex) convertToOrientdbProperty(field.getType(),
                        value, session, parserHolder, converted);
                addEdge(oVertex, diaNode, edgeName, edgeDirection);
            }
        } else if (oType == EMBEDDEDLIST || oType == EMBEDDEDSET) {
            List<OVertex> newEdge = getNewEdge(value, session, converted);
            for (OEdge old : oVertex.getEdges(edgeDirection, edgeName)) {
                if (newEdge.contains(old.getVertex(edgeDirection.opposite()))) {
                    newEdge.remove(old.getVertex(edgeDirection.opposite()));
                } else {
                    old.delete();
                }
            }
            for (OVertex to : newEdge) {
                addEdge(oVertex, to, edgeName, edgeDirection);
            }
        } else {
            throw new EntityConvertException(oType.name() + " type is not support for edge");
        }
    }

    /**
     * Converts a object collection to a {@link OVertex} list.
     *
     * @param value
     * @param session
     * @param converted
     * @return
     */
    private List<OVertex> getNewEdge(final Object value, final ODatabaseSession session,
                                     final Map<Object, OElement> converted) {
        if (value == null) {
            return new ArrayList<>();
        }
        Class gType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        return ((Collection<Object>) value).stream()
                .map(obj -> (OVertex) convertToOrientdbProperty(gType, obj, session, parserHolder, converted))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new {@link OEdge}.
     *
     * @param oVertex
     * @param diaNode
     * @param edgeName
     * @param edgeDirection
     */
    private void addEdge(final OVertex oVertex, final OVertex diaNode,
                         final String edgeName, final ODirection edgeDirection) {

        if (edgeDirection == ODirection.OUT) {
            oVertex.addEdge(diaNode, edgeName);
        } else {
            diaNode.addEdge(oVertex, edgeName);
        }
    }

    /**
     * Gets the edge name.
     *
     * @return
     */
    private String getEdgeName() {
        Edge edge = field.getAnnotation(Edge.class);
        String edgeName = edge.name();
        if (StringUtils.isEmpty(edgeName)) {
            edgeName = StringUtils.capitalize(field.getName());
        }
        return edgeName;
    }

    /**
     * Gets the edge direction.
     *
     * @return
     */
    private ODirection getEdgeDirection() {
        Edge edge = field.getAnnotation(Edge.class);
        if (edge.direction().equals(Edge.INCOMING)) {
            return ODirection.IN;
        }
        return ODirection.OUT;
    }

    /*
     * (non-Javadoc)
     * @see PropertyHandler#getPropertyInJavaType
     */
    @Override
    public Object getPropertyInJavaType(final OElement oElement, final Map<OElement, Object> converted) {
        OVertex oVertex = oElement.asVertex().orElseThrow(() -> new EntityConvertException("Must be a OVertex"));
        if (isEdge) {
            Iterable<OEdge> edges = oVertex.getEdges(getEdgeDirection(), getEdgeName());
            if (oType == EMBEDDED) {
                for (OEdge oEdge : edges) {
                    return convertToJavaProperty(parserHolder, field.getType(), oEdge.getTo(), converted);
                }
            } else if (oType == EMBEDDEDLIST) {
                return convertCollection(edges, new ArrayList<>(),
                        (type, obj) -> convertToJavaProperty(parserHolder, type, ((OEdge) obj).getTo(), converted));
            } else if (oType == EMBEDDEDSET) {
                return convertCollection(edges, new HashSet<>(),
                        (type, obj) -> convertToJavaProperty(parserHolder, type, ((OEdge) obj).getTo(), converted));
            }
        }
        if (oType == EMBEDDED) {
            return convertToJavaProperty(parserHolder, field.getType(), oElement.getProperty(getPropertyName()),
                    converted);
        }
        if (oType == EMBEDDEDMAP) {
            return convertMap(oElement.getProperty(getPropertyName()),
                    (type, obj) -> convertToJavaProperty(parserHolder, type, obj, converted));
        }
        return OType.convert(oElement.getProperty(getPropertyName()), field.getType());
    }

    /*
     * (non-Javadoc)
     * @see PropertyHandler#isCascade
     */
    @Override
    public boolean isCascade() {
        return isCascade;
    }
}
