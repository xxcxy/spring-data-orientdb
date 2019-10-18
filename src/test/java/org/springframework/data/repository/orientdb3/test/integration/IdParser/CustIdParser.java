package org.springframework.data.repository.orientdb3.test.integration.IdParser;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import org.springframework.data.repository.orientdb3.repository.support.OrientdbIdParser;

public class CustIdParser implements OrientdbIdParser<CustId> {
    @Override
    public ORID parseJavaId(final CustId custId) {
        return new ORecordId(custId.getPrefix(), custId.getContent());
    }

    @Override
    public CustId parseOrientdbId(final ORID orid) {
        return new CustId(orid.getClusterId(), orid.getClusterPosition());
    }

    @Override
    public Class<CustId> getIdClass() {
        return CustId.class;
    }
}
