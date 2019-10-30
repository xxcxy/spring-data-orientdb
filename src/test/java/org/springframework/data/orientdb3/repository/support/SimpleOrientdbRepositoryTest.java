package org.springframework.data.orientdb3.repository.support;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleOrientdbRepositoryTest {
    @Mock
    private OrientdbEntityInformation<Object, String> entityInformation;
    @Mock
    private OrientdbEntityManager em;

    private SimpleOrientdbRepository<Object, String> repository;

    @Before
    public void setup() {
        repository = new SimpleOrientdbRepository<>(entityInformation, em);
    }

    @Test
    public void should_find_all_with_sort() {
        List<Object> list = Arrays.asList("", "", "");
        when(entityInformation.getEntityName()).thenReturn("SimpleObject");
        when(em.doQuery("select from SimpleObject order by name ASC", new HashMap<>(), entityInformation))
                .thenReturn(list);

        List<Object> find = repository.findAll(Sort.by("name"));

        assertThat(find, is(list));
    }

    @Test
    public void should_find_all_with_page() {
        List<Object> list = Arrays.asList("", "", "", "", "");
        when(entityInformation.getEntityName()).thenReturn("SimpleObject");
        when(em.doQuery("select from SimpleObject order by UNSORTED skip 5 limit 5", new HashMap<>(),
                entityInformation)).thenReturn(list);
        when(em.count(entityInformation)).thenReturn(20L);

        Page<Object> find = repository.findAll(PageRequest.of(1, 5));

        assertThat(find.getContent(), is(list));
        assertThat(find.getTotalElements(), is(20L));
        assertThat(find.getTotalPages(), is(4));
    }

    @Test
    public void should_save_entity() {
        repository.save("object");
        verify(em).persist("object", entityInformation);
    }

    @Test
    public void should_save_in_particular_cluster() {
        repository.save("object", "cluster");
        verify(em).persist("object", "cluster", entityInformation);
    }

    @Test
    public void should_find_by_id() {
        when(em.find("id", entityInformation)).thenReturn(new Object());
        assertThat(repository.findById("id").isPresent(), is(true));
    }

    @Test
    public void should_count() {
        when(em.count(entityInformation)).thenReturn(20L);
        assertThat(repository.count(), is(20L));
    }

    @Test
    public void should_delete_by_id() {
        Object obj = new Object();
        when(em.find("id", entityInformation)).thenReturn(obj);

        repository.deleteById("id");

        verify(em).remove(obj);
    }
}
