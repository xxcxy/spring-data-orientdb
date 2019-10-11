package io.xxcxy.spring.data.orientdb.test;

import io.xxcxy.spring.data.orientdb.repository.OrientdbEntity;
import io.xxcxy.spring.data.orientdb.repository.OrientdbId;

@OrientdbEntity
public class BenchmarkObject {

    @OrientdbId
    private String dId;

    private String name;

    private String email;

    public BenchmarkObject() {
    }

    public BenchmarkObject(final String name, final String email) {
        this.name = name;
        this.email = email;
    }

    public void setdId(final String dId) {
        this.dId = dId;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getdId() {
        return dId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
