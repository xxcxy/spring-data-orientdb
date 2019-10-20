package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.ElementEntity;
import org.springframework.data.orientdb3.repository.OrientdbId;

@ElementEntity
public class SimpleElement {
    @OrientdbId
    private String id;

    private String value;

    public void setId(final String id) {
        this.id = id;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
}
