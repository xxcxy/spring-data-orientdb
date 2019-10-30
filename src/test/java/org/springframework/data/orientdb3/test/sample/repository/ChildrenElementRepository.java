package org.springframework.data.orientdb3.test.sample.repository;

import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.repository.Query;
import org.springframework.data.orientdb3.test.sample.ChildrenElement;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChildrenElementRepository extends OrientdbRepository<ChildrenElement, String> {

    @Query("select from ChildrenElement")
    List<ChildrenElement> getAllElementObject();

    @Query("select from ChildrenElement where childName = :cName")
    Optional<ChildrenElement> findByName(@Param("cName") String cName);
}
