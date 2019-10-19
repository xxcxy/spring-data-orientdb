package org.springframework.data.repository.orientdb3.test.sample;

import org.springframework.data.repository.orientdb3.repository.Edge;
import org.springframework.data.repository.orientdb3.repository.OrientdbId;
import org.springframework.data.repository.orientdb3.repository.VertexEntity;

@VertexEntity
public class VertexObject {

    @OrientdbId
    private String id;
    private String type;
    @Edge
    private VertexTarget target;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public VertexTarget getTarget() {
        return target;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setTarget(final VertexTarget target) {
        this.target = target;
    }
}
