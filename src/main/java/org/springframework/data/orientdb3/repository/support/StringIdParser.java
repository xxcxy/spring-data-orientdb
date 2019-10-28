package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;

/**
 * A IdParser for String.
 *
 * @author xxcxy
 */
public class StringIdParser implements OrientdbIdParser<String> {
    /*
     * (non-Javadoc)
     * @see OrientdbIdParser#getIdClass()
     */
    @Override
    public Class<String> getIdClass() {
        return String.class;
    }

    /*
     * (non-Javadoc)
     * @see OrientdbIdParser#parseJavaId()
     */
    @Override
    public ORID parseJavaId(final String s) {
        return new ORecordId(s);
    }

    /*
     * (non-Javadoc)
     * @see OrientdbIdParser#parseOrientdbId()
     */
    @Override
    public String parseOrientdbId(final ORID orid) {
        return ORecordId.generateString(orid.getClusterId(), orid.getClusterPosition());
    }
}
