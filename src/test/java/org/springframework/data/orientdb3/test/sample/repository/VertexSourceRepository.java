package org.springframework.data.orientdb3.test.sample.repository;

import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.test.sample.VertexSource;

public interface VertexSourceRepository extends OrientdbRepository<VertexSource, String> {
}
