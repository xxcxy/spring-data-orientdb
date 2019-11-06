package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.Edge;
import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.repository.VertexEntity;

@VertexEntity
public class VertexInterrelatedOne {
    @OrientdbId
    private String id;
    private String name;
    @Edge(cascade = true)
    private VertexInterrelatedTwo two;

    public VertexInterrelatedTwo getTwo() {
        return two;
    }

    public void setTwo(final VertexInterrelatedTwo two) {
        this.two = two;
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
