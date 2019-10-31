package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.data.orientdb3.repository.FromVertex;
import org.springframework.data.orientdb3.repository.ToVertex;
import org.springframework.data.orientdb3.repository.exception.EntityConvertException;
import org.springframework.data.orientdb3.repository.exception.EntityInitException;

import java.lang.reflect.Field;
import java.util.Map;

import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDED;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDMAP;

/**
 * Extension of {@link PropertyHandler} that handle Edge's property.
 *
 * @author xxcxy
 */
public class EdgePropertyHandler extends PropertyHandler {

    private final Field field;
    private final OrientdbIdParserHolder parserHolder;
    private final boolean isFrom;
    private final boolean isTo;
    private final OType oType;

    /**
     * Creates a new {@link EdgePropertyHandler}
     *
     * @param field
     * @param parserHolder
     */
    public EdgePropertyHandler(final Field field, final OrientdbIdParserHolder parserHolder) {
        super(field);
        this.field = field;
        this.parserHolder = parserHolder;
        this.oType = getOrientdbType();

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

    /*
     * (non-Javadoc)
     * @see PropertyHandler#setOElementProperty
     */
    @Override
    public void setOElementProperty(final OElement oElement, final Object value, final ODatabaseSession session,
                                    final Map<Object, OElement> converted) {
        if (isFrom || isTo) {
            return;
        }
        String propertyName = getPropertyName();
        oElement.removeProperty(propertyName);
        if (value == null) {
            return;
        }
        if (oType == EMBEDDED) {
            oElement.setProperty(propertyName,
                    convertToOrientdbProperty(field.getType(), value, session, parserHolder, converted), oType);
        } else if (oType == EMBEDDEDMAP) {
            Map<String, Object> map = convertMap((Map) value,
                    (type, obj) -> convertToOrientdbProperty(type, obj, session, parserHolder, converted));
            oElement.setProperty(propertyName, map, oType);
        } else {
            oElement.setProperty(propertyName, value, oType);
        }
    }

    /*
     * (non-Javadoc)
     * @see PropertyHandler#getPropertyInJavaType
     */
    @Override
    public Object getPropertyInJavaType(final OElement oElement, final Map<OElement, Object> converted) {
        if (isFrom) {
            OEdge oEdge = oElement.asEdge().orElseThrow(() -> new EntityConvertException("Must be a OEdge"));
            return convertToJavaProperty(parserHolder, field.getType(), oEdge.getFrom(), converted);
        }
        if (isTo) {
            OEdge oEdge = oElement.asEdge().orElseThrow(() -> new EntityConvertException("Must be a OEdge"));
            return convertToJavaProperty(parserHolder, field.getType(), oEdge.getTo(), converted);
        }
        if (oType == EMBEDDED) {
            return convertToJavaProperty(parserHolder, field.getType(),
                    oElement.getProperty(getPropertyName()), converted);
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
        return false;
    }
}
