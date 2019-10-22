package org.springframework.data.orientdb3.test.sample.repository;

import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.test.sample.VertexWithEdges;

public interface VertexWithEdgesRepository extends OrientdbRepository<VertexWithEdges, String> {
}
