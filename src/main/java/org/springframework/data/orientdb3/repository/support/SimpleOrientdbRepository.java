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

@Repository
@Transactional(readOnly = true)
public class SimpleOrientdbRepository<T, ID> implements OrientdbRepository<T, ID> {
    private final OrientdbEntityInformation<T, ID> entityInformation;
    private final OrientdbEntityManager em;

    public SimpleOrientdbRepository(final OrientdbEntityInformation<T, ID> entityInformation,
                                    final OrientdbEntityManager em) {
        this.entityInformation = entityInformation;
        this.em = em;
    }

    @Override
    public List<T> findAll(final Sort sort) {
        return em.createQuery(QueryUtils.createSortQuery(sort), entityInformation).getResultList();
    }

    @Override
    public Page<T> findAll(final Pageable pageable) {
        List<T> pageContent = em.createQuery(QueryUtils.createPageQuery(pageable), entityInformation).getResultList();
        if (pageable.isPaged()) {
            return PageableExecutionUtils.getPage(pageContent, pageable, () -> em.count(entityInformation));
        }
        return new PageImpl<>(pageContent);
    }

    @Transactional
    @Override
    public <S extends T> S save(final S s) {
        em.persist(s, entityInformation);
        return s;
    }

    @Transactional
    @Override
    public <S extends T> Iterable<S> saveAll(final Iterable<S> entities) {
        Assert.notNull(entities, "entities must not be null!");

        for (S entity : entities) {
            em.persist(entity, entityInformation);
        }
        return entities;
    }

    @Transactional
    @Override
    public <S extends T> S save(final S s, final String clusterName) {
        em.persist(s, clusterName, entityInformation);
        return s;
    }

    @Transactional
    @Override
    public <S extends T> List<S> saveAll(final List<S> entities, final String clusterName) {
        Assert.notNull(entities, "entities must not be null!");

        for (S entity : entities) {
            em.persist(entity, clusterName, entityInformation);
        }
        return entities;
    }

    @Override
    public List<T> findAll(final String clusterName) {
        return em.findAll(clusterName, entityInformation);
    }

    @Override
    public Optional<T> findById(final ID id) {
        Assert.notNull(id, "Id must not be null!");

        return Optional.ofNullable(em.find(id, entityInformation));
    }

    @Override
    public boolean existsById(final ID id) {
        Assert.notNull(id, "Id must not be null!");

        return findById(id).isPresent();
    }

    @Override
    public List<T> findAll() {
        return em.findAll(entityInformation);
    }

    @Override
    public List<T> findAllById(final Iterable<ID> ids) {
        Assert.notNull(ids, "Ids must not be null!");

        List<T> result = new ArrayList<>();
        for (ID id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public long count() {
        return em.count(entityInformation);
    }

    @Transactional
    @Override
    public void deleteById(final ID id) {
        Assert.notNull(id, "Id must not be null!");

        delete(findById(id).orElseThrow(() -> new EmptyResultDataAccessException(
                String.format("No %s entity with id %s exists!", entityInformation.getJavaType(), id), 1)));
    }

    @Transactional
    @Override
    public void delete(final T entity) {
        Assert.notNull(entity, "Entity must not be null!");

        em.remove(entity, entityInformation);
    }

    @Transactional
    @Override
    public void deleteAll(final Iterable<? extends T> entities) {
        Assert.notNull(entities, "Entities must not be null!");

        for (T t : entities) {
            delete(t);
        }
    }

    @Transactional
    @Override
    public void deleteAll() {
        for (T element : findAll()) {
            delete(element);
        }
    }
}
