package org.springframework.data.orientdb3.test.sample.repository;

import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.test.sample.EdgeObject;

public interface EdgeObjectRepository extends OrientdbRepository<EdgeObject, String> {
}
