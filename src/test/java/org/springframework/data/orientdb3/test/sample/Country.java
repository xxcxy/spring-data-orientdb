package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.repository.VertexEntity;

@VertexEntity(name = "Countries")
public class Country {
    @OrientdbId
    private String id;
    private String name;

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
