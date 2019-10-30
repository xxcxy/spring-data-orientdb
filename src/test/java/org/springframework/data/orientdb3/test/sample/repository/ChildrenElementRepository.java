package org.springframework.data.orientdb3.test.sample.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.repository.Query;
import org.springframework.data.orientdb3.test.sample.ChildrenElement;
import org.springframework.data.orientdb3.test.sample.ChildrenProjection;
import org.springframework.data.orientdb3.test.sample.ProjectionObject;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChildrenElementRepository extends OrientdbRepository<ChildrenElement, String> {

    @Query("select from ChildrenElement")
    List<ChildrenElement> getAllElementObject();

    @Query("select from ChildrenElement where childName = :cName")
    Optional<ChildrenElement> findByName(@Param("cName") String cName);

    @Query("select childName as cName, parentName as pName from ChildrenElement")
    List<ProjectionObject> getAllAsProjection();


    @Query(value = "select childName as cName, parentName as pName from ChildrenElement where parentName = :pName",
            countQuery = "select count(*) as count from ChildrenElement where parentName = :pName")
    Page<ProjectionObject> getAsProjectionWithParentName(@Param("pName") String pName, Pageable pageable);

    List<ChildrenProjection> findByChildNameBetween(String start, String end);
}
