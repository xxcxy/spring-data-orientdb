package org.springframework.data.orientdb3.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.lang.Nullable;

/**
 * A entity proxy interface that label a entity is a entity proxy.
 *
 * @author xxcxy
 */
public interface EntityProxyInterface {
    /**
     * Saves the {@link OElement}.
     *
     * @param session
     * @param cluster
     * @return
     */
    OElement saveOElement(ODatabaseSession session, @Nullable String cluster);

    /**
     * Gets the {@link OElement} from the entity proxy.
     *
     * @return
     */
    OElement findOElement();

    /**
     * Deletes the {@link OElement}.
     */
    void deleteOElement();

    /**
     * Loads value to target field.
     */
    void loadStable();

    /**
     * Loads id to target field.
     */
    void loadId();
}
