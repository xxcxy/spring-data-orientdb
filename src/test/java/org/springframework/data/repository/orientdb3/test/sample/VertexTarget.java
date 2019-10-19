package org.springframework.data.repository.orientdb3.test.sample;

import org.springframework.data.repository.orientdb3.repository.OrientdbId;
import org.springframework.data.repository.orientdb3.repository.VertexEntity;

@VertexEntity
public class VertexTarget {
    @OrientdbId
    private String id;

    private String type;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
