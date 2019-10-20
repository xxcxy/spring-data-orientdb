package org.springframework.data.orientdb3.test.sample.repository;

import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.test.sample.ElementObject;

public interface ElementObjectRepository extends OrientdbRepository<ElementObject, String> {
}
