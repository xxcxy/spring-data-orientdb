package org.springframework.data.orientdb3.test.sample.repository;

import org.springframework.data.orientdb3.repository.Modifying;
import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.repository.Query;
import org.springframework.data.orientdb3.test.sample.CommandElement;

import java.util.Optional;

public interface CommandElementRepository extends OrientdbRepository<CommandElement, String> {

    @Modifying
    @Query("update CommandElement set name= ? where name = ?")
    void updateNameByName(String newName, String oldName);

    @Modifying
    @Query("delete from CommandElement where name= ?")
    void deleteByName(String name);

    Optional<CommandElement> findByName(String s);
}
