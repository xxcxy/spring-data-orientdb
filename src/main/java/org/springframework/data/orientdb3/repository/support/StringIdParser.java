package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

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
        int index = s.indexOf('-', 1);
        return new ORecordId(parseInt(s.substring(0, index)), parseInt(s.substring(index + 1)));
    }

    /*
     * (non-Javadoc)
     * @see OrientdbIdParser#parseOrientdbId()
     */
    @Override
    public String parseOrientdbId(final ORID orid) {
        return String.join("-", valueOf(orid.getClusterId()), valueOf(orid.getClusterPosition()));
    }
}
