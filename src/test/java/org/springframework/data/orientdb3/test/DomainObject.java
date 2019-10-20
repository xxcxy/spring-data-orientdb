package org.springframework.data.orientdb3.test;

import org.springframework.data.orientdb3.repository.ElementEntity;
import org.springframework.data.orientdb3.repository.EntityProperty;
import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.test.integration.IdParser.CustId;

@ElementEntity
public class DomainObject {

    @OrientdbId
    private CustId dId;

    private String p1;

    @EntityProperty(name = "cp2")
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
