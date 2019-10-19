package org.springframework.data.repository.orientdb3.test.sample.repository;

import org.springframework.data.repository.orientdb3.repository.OrientdbRepository;
import org.springframework.data.repository.orientdb3.test.sample.ElementObject;

public interface ElementObjectRepository extends OrientdbRepository<ElementObject, String> {
}
