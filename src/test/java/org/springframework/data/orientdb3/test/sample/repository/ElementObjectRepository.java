package org.springframework.data.orientdb3.test.sample.repository;

import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.test.sample.ElementObject;
import org.springframework.data.orientdb3.test.sample.ProjectionNested;

import java.util.List;

public interface ElementObjectRepository extends OrientdbRepository<ElementObject, String> {
    List<ProjectionNested> findByType(String type);
}
