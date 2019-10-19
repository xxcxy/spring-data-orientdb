package org.springframework.data.repository.orientdb3.test.sample;

import org.springframework.data.repository.orientdb3.repository.EdgeEntity;
import org.springframework.data.repository.orientdb3.repository.FromVertex;
import org.springframework.data.repository.orientdb3.repository.OrientdbId;
import org.springframework.data.repository.orientdb3.repository.ToVertex;

@EdgeEntity
public class EdgeObject {
    @OrientdbId
    private String id;
    @FromVertex
    private VertexSource source;
    @ToVertex
    private VertexTarget target;
    private String type;
    private long length;

    public void setId(final String id) {
        this.id = id;
    }

    public void setSource(final VertexSource source) {
        this.source = source;
    }

    public void setTarget(final VertexTarget target) {
        this.target = target;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setLength(final long length) {
        this.length = length;
    }

    public String getId() {
        return id;
    }

    public VertexSource getSource() {
        return source;
    }

    public VertexTarget getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }

    public long getLength() {
        return length;
    }
}
