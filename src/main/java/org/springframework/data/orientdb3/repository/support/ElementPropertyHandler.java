package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.data.orientdb3.repository.Embedded;
import org.springframework.data.orientdb3.repository.Link;
import org.springframework.data.orientdb3.repository.exception.EntityConvertException;
import org.springframework.data.orientdb3.repository.exception.EntityInitException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDED;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDMAP;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINK;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINKLIST;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINKMAP;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINKSET;
import static org.springframework.data.orientdb3.repository.util.Constants.OBJECT_TYPE;

/**
 * Extension of {@link PropertyHandler} that handle Entity's property.
 *
 * @author xxcxy
 */
public class ElementPropertyHandler extends PropertyHandler {
    private final Field field;
    private final OrientdbIdParserHolder parserHolder;
    private final boolean isLink;
    private final boolean isEmbedded;
    private final boolean isCascade;
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
            isCascade = field.getAnnotation(Link.class).cascade();
        } else {
            isLink = false;
            isCascade = false;
        }

        if (isLink && isEmbedded) {
            throw new EntityInitException("A Entity property must not be both embedded and link");
        }

        // After set isEmbedded or isLink
        this.oType = getOrientdbType();
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
        if (value == null) {
            return;
        }
        if (oType == EMBEDDED || oType == LINK) {
            oElement.setProperty(propertyName, convertToOrientdbProperty(field.getType(),
                    value, session, parserHolder, converted));
        } else if (oType == LINKLIST) {
            Collection list = convertCollection((Collection) value, new ArrayList<>(),
                    (type, obj) -> convertToOrientdbProperty(type, obj, session, parserHolder, converted));
            oElement.setProperty(propertyName, list, oType);
        } else if (oType == LINKSET) {
            Collection set = convertCollection((Collection) value, new HashSet<>(),
                    (type, obj) -> convertToOrientdbProperty(type, obj, session, parserHolder, converted));
            oElement.setProperty(propertyName, set, oType);
        } else if (oType == LINKMAP || oType == EMBEDDEDMAP) {
            Map<String, Object> map = convertMap((Map) value,
                    (type, obj) -> convertToOrientdbProperty(type, obj, session, parserHolder, converted));
            oElement.setProperty(propertyName, map, oType);
        } else {
            oElement.setProperty(propertyName, value, oType);
        }
    }


    /**
     * Changes the field java type to OType using OType.getTypeByClass
     *
     * @return
     */
    public OType getOrientdbType() {
        OType oType = super.getOrientdbType();
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

    /*
     * (non-Javadoc)
     * @see PropertyHandler#getPropertyInJavaType
     */
    public Object getPropertyInJavaType(final OElement oElement, final Map<OElement, Object> converted) {
        if (oType == EMBEDDED || oType == LINK) {
            return convertToJavaProperty(parserHolder, field.getType(),
                    oElement.getProperty(getPropertyName()), converted);
        }
        // Every element has it's own converter so can't use OType.convert
        if (oType == LINKLIST) {
            return convertCollection(oElement.getProperty(getPropertyName()), new ArrayList(),
                    (type, obj) -> convertToJavaProperty(parserHolder, type, obj, converted));
        }
        if (oType == LINKSET) {
            return convertCollection(oElement.getProperty(getPropertyName()), new HashSet<>(),
                    (type, obj) -> convertToJavaProperty(parserHolder, type, obj, converted));
        }
        if (oType == LINKMAP || oType == EMBEDDEDMAP) {
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
