package org.springframework.data.repository.orientdb3.test.sample.repository;

import org.springframework.data.repository.orientdb3.repository.OrientdbRepository;
import org.springframework.data.repository.orientdb3.test.sample.ChildrenElement;

public interface ChildrenElementRepository extends OrientdbRepository<ChildrenElement, String> {
}
