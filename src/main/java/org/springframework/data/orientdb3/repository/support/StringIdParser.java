package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;

public class StringIdParser implements OrientdbIdParser<String> {
    @Override
    public Class<String> getIdClass() {
        return String.class;
    }

    @Override
    public ORID parseJavaId(final String s) {
        return new ORecordId(s);
    }

    @Override
    public String parseOrientdbId(final ORID orid) {
        return ORecordId.generateString(orid.getClusterId(), orid.getClusterPosition());
    }
}
