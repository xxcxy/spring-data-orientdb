package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.Edge;
import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.repository.VertexEntity;

@VertexEntity
public class VertexInterrelatedTwo {
    @OrientdbId
    private String id;
    private String name;
    @Edge
    private VertexInterrelatedOne one;

    public VertexInterrelatedOne getOne() {
        return one;
    }

    public void setOne(final VertexInterrelatedOne one) {
        this.one = one;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
