package io.xxcxy.spring.data.orientdb.test.integration.IdParser;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import io.xxcxy.spring.data.orientdb.repository.support.OrientdbIdParser;

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
