package org.springframework.data.orientdb3.test.sample;

import lombok.Data;
import org.springframework.data.orientdb3.repository.QueryResult;

@Data
@QueryResult
public class ChildrenProjection {
    private String childName;
    private String parentName;
}
