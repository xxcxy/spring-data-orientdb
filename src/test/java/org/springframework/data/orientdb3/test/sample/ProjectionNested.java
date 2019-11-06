package org.springframework.data.orientdb3.test.sample;

import lombok.Data;
import org.springframework.data.orientdb3.repository.QueryResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@QueryResult
public class ProjectionNested {
    private Long size;
    private String type;
    private List<String> names;
    private Set<String> sets;
    private Map<String, Long> maps;
    private List<ProjectionValue> elementList;
    private Set<ProjectionValue> elementSet;
    private Map<String, ProjectionValue> elementMap;
    private ProjectionPojo pojo;
    private EnValue enValue;
}
