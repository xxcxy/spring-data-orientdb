package org.springframework.data.repository.orientdb3.test.sample.repository;

import org.springframework.data.repository.orientdb3.repository.OrientdbRepository;
import org.springframework.data.repository.orientdb3.test.sample.VertexObject;

public interface VertexObjectRepository extends OrientdbRepository<VertexObject, String> {
}
