package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.EmbeddedEntity;

@EmbeddedEntity
public class Pojo {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
