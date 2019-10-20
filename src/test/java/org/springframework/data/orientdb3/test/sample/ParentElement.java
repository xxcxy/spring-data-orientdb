package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.ElementEntity;
import org.springframework.data.orientdb3.repository.OrientdbId;

@ElementEntity
public class ParentElement {
    @OrientdbId
    private String id;
    private String parentName;

    public String getId() {
        return id;
    }

    public String getParentName() {
        return parentName;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setParentName(final String parentName) {
        this.parentName = parentName;
    }
}
