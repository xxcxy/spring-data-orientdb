package org.springframework.data.orientdb3.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.lang.Nullable;

public interface EntityProxyInterface {
    OElement saveOElement(ODatabaseSession session, @Nullable String cluster);

    void deleteOElement();
}
