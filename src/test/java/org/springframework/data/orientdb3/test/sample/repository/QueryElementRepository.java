package org.springframework.data.orientdb3.test.sample.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.test.sample.QueryElement;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface QueryElementRepository extends OrientdbRepository<QueryElement, String> {

    List<QueryElement> findByDateAfter(Date date);

    List<QueryElement> findByDateBefore(Date date);

    List<QueryElement> findByNameContaining(String namePart);

    List<QueryElement> findByEmailAddressesContains(Collection<String> addresses);

    List<QueryElement> findByEmailAddressesContains(String address);

    List<QueryElement> findByNameIn(Iterable<String> names);

    List<QueryElement> findByScoreBetween(double min, double max);

    List<QueryElement> findByNameStartingWith(String nameStart);

    List<QueryElement> findByNameEndingWith(String nameEnd);

    List<QueryElement> findByNameExists();

    List<QueryElement> findByActivatedIsTrue();

    List<QueryElement> findByActivatedIsFalse();

    List<QueryElement> findByNameIs(String name);

    List<QueryElement> findByNameNotNull();

    List<QueryElement> findByNameNull();

    List<QueryElement> findByScoreGreaterThan(double score);

    List<QueryElement> findByScoreGreaterThanEqual(double score);

    List<QueryElement> findByScoreLessThan(double score);

    List<QueryElement> findByScoreLessThanEqual(double score);

    List<QueryElement> findByNameLike(String name);

    List<QueryElement> findByNameNotLike(String name);

    List<QueryElement> findByNameRegex(String regex);

    List<QueryElement> findByNameAndDescription(String name, String description);

    List<QueryElement> findByNameOrDescription(String name, String description);

    QueryElement findEntityByName(String name);

    Optional<QueryElement> findOptionByName(String s);

    Page<QueryElement> findByName(String name, Pageable page);

    List<QueryElement> findSortByName(String name, Sort sort);

    Iterable<QueryElement> findIterableByName(String name);

    Stream<QueryElement> findStreamByName(String name);
}
