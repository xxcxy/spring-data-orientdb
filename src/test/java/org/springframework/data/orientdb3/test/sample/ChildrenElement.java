package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.ElementEntity;

@ElementEntity
public class ChildrenElement extends ParentElement {
    private String childName;

    public String getChildName() {
        return childName;
    }

    public void setChildName(final String childName) {
        this.childName = childName;
    }
}
