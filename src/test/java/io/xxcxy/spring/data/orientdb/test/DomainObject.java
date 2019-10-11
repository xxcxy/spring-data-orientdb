package io.xxcxy.spring.data.orientdb.test;

import io.xxcxy.spring.data.orientdb.repository.OrientdbEntity;
import io.xxcxy.spring.data.orientdb.repository.OrientdbId;
import io.xxcxy.spring.data.orientdb.repository.OrientdbProperty;
import io.xxcxy.spring.data.orientdb.test.integration.IdParser.CustId;

@OrientdbEntity
public class DomainObject {

    @OrientdbId
    private CustId dId;

    private String p1;

    @OrientdbProperty(name = "cp2")
    private String p2;

    public DomainObject() {
    }

    public DomainObject(final String p1, final String p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public CustId getdId() {
        return dId;
    }

    public String getP1() {
        return p1;
    }

    public String getP2() {
        return p2;
    }
}
