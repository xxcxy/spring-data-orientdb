package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.ElementEntity;
import org.springframework.data.orientdb3.repository.OrientdbId;

@ElementEntity
public class CommandElement {
    @OrientdbId
    private String id;
    private String name;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
