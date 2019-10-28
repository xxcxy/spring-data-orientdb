package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.Edge;
import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.repository.VertexEntity;

@VertexEntity
public class VertexObject {

    @OrientdbId
    private String id;
    private String type;
    @Edge(name = "TestOutgoing")
    private VertexTarget target;

    @Edge(direction = Edge.INCOMING, name = "TestIncoming")
    private VertexSource source;

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

    public VertexSource getSource() {
        return source;
    }

    public void setSource(final VertexSource source) {
        this.source = source;
    }

    public void setTarget(final VertexTarget target) {
        this.target = target;
    }
}
