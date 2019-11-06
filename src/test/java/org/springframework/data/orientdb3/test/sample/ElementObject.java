package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.ElementEntity;
import org.springframework.data.orientdb3.repository.Embedded;
import org.springframework.data.orientdb3.repository.EntityProperty;
import org.springframework.data.orientdb3.repository.Index;
import org.springframework.data.orientdb3.repository.Link;
import org.springframework.data.orientdb3.repository.OrientdbId;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ElementEntity(indexes = {@Index(name = "ElementObject.lengthAndType", columnList = "size,type",
        type = Index.NOTUNIQUE),
        @Index(name = "ElementObject.type", columnList = "type", type = Index.FULLTEXT)})
public class ElementObject {
    @OrientdbId
    private String id;
    @EntityProperty(name = "size", min = "1", max = "10")
    private long length;
    private String type;
    @Embedded
    private List<String> names;
    @Embedded
    private Set<String> sets;
    @Embedded
    private Map<String, Long> maps;
    @Link(cascade = true)
    private List<SimpleElement> elementList;
    @Link(cascade = true)
    private Set<SimpleElement> elementSet;
    @Link(cascade = true)
    private Map<String, SimpleElement> elementMap;
    @Embedded
    private Pojo pojo;
    private EnValue enValue;

    public void setId(final String id) {
        this.id = id;
    }

    public void setLength(final long length) {
        this.length = length;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setNames(final List<String> names) {
        this.names = names;
    }

    public void setSets(final Set<String> sets) {
        this.sets = sets;
    }

    public void setMaps(final Map<String, Long> maps) {
        this.maps = maps;
    }

    public void setElementList(final List<SimpleElement> elementList) {
        this.elementList = elementList;
    }

    public void setElementSet(final Set<SimpleElement> elementSet) {
        this.elementSet = elementSet;
    }

    public void setElementMap(final Map<String, SimpleElement> elementMap) {
        this.elementMap = elementMap;
    }

    public String getId() {
        return id;
    }

    public long getLength() {
        return length;
    }

    public String getType() {
        return type;
    }

    public List<String> getNames() {
        return names;
    }

    public Set<String> getSets() {
        return sets;
    }

    public Map<String, Long> getMaps() {
        return maps;
    }

    public List<SimpleElement> getElementList() {
        return elementList;
    }

    public Set<SimpleElement> getElementSet() {
        return elementSet;
    }

    public Pojo getPojo() {
        return pojo;
    }

    public void setPojo(final Pojo pojo) {
        this.pojo = pojo;
    }

    public Map<String, SimpleElement> getElementMap() {
        return elementMap;
    }

    public EnValue getEnValue() {
        return enValue;
    }

    public void setEnValue(final EnValue enValue) {
        this.enValue = enValue;
    }
}
