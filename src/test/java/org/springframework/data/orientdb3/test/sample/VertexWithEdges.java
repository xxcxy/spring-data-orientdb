package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.Edge;
import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.repository.VertexEntity;

import java.util.List;

@VertexEntity
public class VertexWithEdges {
    @OrientdbId
    private String id;
    private String type;
    @Edge
    private List<VertexTarget> targets;

    @Edge
    private VertexSource source;

    public void setId(final String id) {
        this.id = id;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setTargets(final List<VertexTarget> targets) {
        this.targets = targets;
    }

    public void setSource(final VertexSource source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public List<VertexTarget> getTargets() {
        return targets;
    }

    public VertexSource getSource() {
        return source;
    }
}
