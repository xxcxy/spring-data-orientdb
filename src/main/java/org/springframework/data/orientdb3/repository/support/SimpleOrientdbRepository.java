package org.springframework.data.orientdb3.repository.support;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.repository.query.QueryUtils;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of the {@link org.springframework.data.repository.CrudRepository} interface.
 *
 * @param <T>  the type of the entity to handle
 * @param <ID> the type of the entity's identifier
 * @author xxcxy
 */
@Repository
@Transactional(readOnly = true)
public class SimpleOrientdbRepository<T, ID> implements OrientdbRepository<T, ID> {
    private final OrientdbEntityInformation<T, ID> entityInformation;
    private final OrientdbEntityManager em;

    /**
     * Creates a new {@link SimpleOrientdbRepository}.
     *
     * @param entityInformation must not be {@literal null}.
     * @param em                must not be {@literal null}.
     */
    public SimpleOrientdbRepository(final OrientdbEntityInformation<T, ID> entityInformation,
                                    final OrientdbEntityManager em) {
        this.entityInformation = entityInformation;
        this.em = em;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll(Sort)
     */
    @Override
    public List<T> findAll(final Sort sort) {
        return em.createQuery(QueryUtils.createSortQuery(sort), entityInformation).getResultList();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll(Pageable)
     */
    @Override
    public Page<T> findAll(final Pageable pageable) {
        List<T> pageContent = em.createQuery(QueryUtils.createPageQuery(pageable), entityInformation).getResultList();
        if (pageable.isPaged()) {
            return PageableExecutionUtils.getPage(pageContent, pageable, () -> em.count(entityInformation));
        }
        return new PageImpl<>(pageContent);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#save(Object)
     */
    @Transactional
    @Override
    public <S extends T> S save(final S s) {
        return (S) em.persist(s, entityInformation);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#saveAll(Iterable)
     */
    @Transactional
    @Override
    public <S extends T> Iterable<S> saveAll(final Iterable<S> entities) {
        Assert.notNull(entities, "entities must not be null!");

        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) em.persist(entity, entityInformation));
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see OrientdbRepository#save(Object,String)
     */
    @Transactional
    @Override
    public <S extends T> S save(final S s, final String clusterName) {
        return (S) em.persist(s, clusterName, entityInformation);
    }

    /*
     * (non-Javadoc)
     * @see OrientdbRepository#saveAll(List,String)
     */
    @Transactional
    @Override
    public <S extends T> List<S> saveAll(final List<S> entities, final String clusterName) {
        Assert.notNull(entities, "entities must not be null!");

        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) em.persist(entity, clusterName, entityInformation));
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see OrientdbRepository#findAll(String)
     */
    @Override
    public List<T> findAll(final String clusterName) {
        return em.findAll(clusterName, entityInformation);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findById(ID)
     */
    @Override
    public Optional<T> findById(final ID id) {
        Assert.notNull(id, "Id must not be null!");

        return Optional.ofNullable(em.find(id, entityInformation));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#existsById(ID)
     */
    @Override
    public boolean existsById(final ID id) {
        Assert.notNull(id, "Id must not be null!");

        return findById(id).isPresent();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll()
     */
    @Override
    public List<T> findAll() {
        return em.findAll(entityInformation);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAllById(Iterable)
     */
    @Override
    public List<T> findAllById(final Iterable<ID> ids) {
        Assert.notNull(ids, "Ids must not be null!");

        List<T> result = new ArrayList<>();
        for (ID id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#count()
     */
    @Override
    public long count() {
        return em.count(entityInformation);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#deleteById(ID)
     */
    @Transactional
    @Override
    public void deleteById(final ID id) {
        Assert.notNull(id, "Id must not be null!");

        delete(findById(id).orElseThrow(() -> new EmptyResultDataAccessException(
                String.format("No %s entity with id %s exists!", entityInformation.getJavaType(), id), 1)));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#delete(Object)
     */
    @Transactional
    @Override
    public void delete(final T entity) {
        Assert.notNull(entity, "Entity must not be null!");

        em.remove(entity);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#deleteAll(Iterable)
     */
    @Transactional
    @Override
    public void deleteAll(final Iterable<? extends T> entities) {
        Assert.notNull(entities, "Entities must not be null!");

        for (T t : entities) {
            delete(t);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#deleteAll()
     */
    @Transactional
    @Override
    public void deleteAll() {
        em.createCommand(QueryUtils.createDeleteAllQuery(entityInformation.getEntityType()), entityInformation);
    }
}
