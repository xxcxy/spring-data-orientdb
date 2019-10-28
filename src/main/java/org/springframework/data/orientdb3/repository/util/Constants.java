package org.springframework.data.orientdb3.repository.util;

import com.orientechnologies.orient.core.metadata.schema.OType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDED;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDLIST;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDMAP;
import static com.orientechnologies.orient.core.metadata.schema.OType.EMBEDDEDSET;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINK;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINKLIST;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINKMAP;
import static com.orientechnologies.orient.core.metadata.schema.OType.LINKSET;

/**
 * Helper class to transform type.
 *
 * @author xxcxy
 */
public abstract class Constants {
    public static final Map<OType, OType> OBJECT_TYPE = new HashMap<>();
    public static final Map<Class, OType> TYPES_BY_CLASS = new HashMap<>();

    static {
        OBJECT_TYPE.put(EMBEDDED, LINK);
        OBJECT_TYPE.put(EMBEDDEDLIST, LINKLIST);
        OBJECT_TYPE.put(EMBEDDEDSET, LINKSET);
        OBJECT_TYPE.put(EMBEDDEDMAP, LINKMAP);
        OBJECT_TYPE.put(EMBEDDED, LINK);

        TYPES_BY_CLASS.put(List.class, EMBEDDEDLIST);
        TYPES_BY_CLASS.put(Set.class, EMBEDDEDSET);
        TYPES_BY_CLASS.put(Map.class, EMBEDDEDMAP);
    }
}
