package org.springframework.data.repository.orientdb3.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

@NoRepositoryBean
public interface OrientdbRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
    @Override
    List<T> findAll();

    @Override
    List<T> findAll(Sort sort);

    @Override
    List<T> findAllById(Iterable<ID> ids);

    List<T> findAll(String clusterName);

    <S extends T> S save(S s, String clusterName);

    <S extends T> List<S> saveAll(List<S> iterable, String clusterName);
}
