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
    private VertexTarget reverseTo;
    @ToVertex
    private VertexObject reverseFrom;
    private String type;
    private long length;

    public void setId(final String id) {
        this.id = id;
    }

    public void setReverseTo(final VertexTarget reverseTo) {
        this.reverseTo = reverseTo;
    }

    public void setReverseFrom(final VertexObject reverseFrom) {
        this.reverseFrom = reverseFrom;
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

    public VertexTarget getReverseTo() {
        return reverseTo;
    }

    public VertexObject getReverseFrom() {
        return reverseFrom;
    }

    public String getType() {
        return type;
    }

    public long getLength() {
        return length;
    }
}
