package org.springframework.data.orientdb3.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Orientdb specific extension of {@link org.springframework.data.repository.Repository}.
 *
 * @author xxcxy
 */
@NoRepositoryBean
public interface OrientdbRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll()
     */
    @Override
    List<T> findAll();

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Sort)
     */
    @Override
    List<T> findAll(Sort sort);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.Iterable)
     */
    @Override
    List<T> findAllById(Iterable<ID> ids);

    /**
     * Find a designated cluster's all classes
     *
     * @param clusterName
     * @return
     */
    List<T> findAll(String clusterName);

    /**
     * Save entity to a designated cluster
     *
     * @param entity
     * @param clusterName
     * @param <S>
     * @return
     */
    <S extends T> S save(S entity, String clusterName);

    /**
     * Save entities to a designated cluster
     *
     * @param entities
     * @param clusterName
     * @param <S>
     * @return
     */
    <S extends T> List<S> saveAll(List<S> entities, String clusterName);
}
