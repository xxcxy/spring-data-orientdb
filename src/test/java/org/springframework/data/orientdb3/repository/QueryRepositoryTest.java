package org.springframework.data.orientdb3.repository;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.orientdb3.repository.support.OrientdbIdParserHolder;
import org.springframework.data.orientdb3.repository.support.OrientdbRepositoryFactory;
import org.springframework.data.orientdb3.repository.support.StringIdParser;
import org.springframework.data.orientdb3.support.IOrientdbConfig;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.orientdb3.test.sample.ChildrenElement;
import org.springframework.data.orientdb3.test.sample.ChildrenProjection;
import org.springframework.data.orientdb3.test.sample.ProjectionObject;
import org.springframework.data.orientdb3.test.sample.QueryElement;
import org.springframework.data.orientdb3.test.sample.repository.ChildrenElementRepository;
import org.springframework.data.orientdb3.test.sample.repository.QueryElementRepository;
import org.springframework.test.context.ContextConfiguration;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = QueryRepositoryTest.config.class)
public class QueryRepositoryTest extends RepositoryTestBase {

    private ChildrenElementRepository childrenRepository;
    private QueryElementRepository queryElementRepository;

    @Before
    public void setup() {
        childrenRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(ChildrenElementRepository.class);
        queryElementRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(QueryElementRepository.class);
    }

    @Test
    public void should_find_by_after() {
        Calendar calendar = Calendar.getInstance();
        prepareDate(calendar);
        List<QueryElement> lq = queryElementRepository.findByDateAfter(calendar.getTime());
        assertThat(lq.size(), is(1));
        assertThat(lq.get(0).getName(), is("tomorrow"));
    }

    @Test
    public void should_find_by_before() {
        Calendar calendar = Calendar.getInstance();
        prepareDate(calendar);
        List<QueryElement> lq = queryElementRepository.findByDateBefore(calendar.getTime());
        assertThat(lq.size(), is(1));
        assertThat(lq.get(0).getName(), is("yesterday"));
    }

    @Test
    public void should_find_by_containing() {
        prepareContaining();
        List<QueryElement> lq = queryElementRepository.findByNameContaining("name");
        assertThat(lq.size(), is(1));
        assertThat(lq.get(0).getName(), is("nameContains"));

        List<QueryElement> lr = queryElementRepository.findByNameContaining("Cont");
        assertThat(lr.size(), is(2));

        List<QueryElement> cq = queryElementRepository.findByEmailAddressesContains("email1");
        assertThat(cq.size(), is(1));
        assertThat(cq.get(0).getName(), is("addressContains"));

        List<QueryElement> cr = queryElementRepository.findByEmailAddressesContains(asList("email2", "add1"));
        assertThat(cr.size(), is(2));
    }

    private void prepareContaining() {
        QueryElement t = new QueryElement();
        t.setName("nameContains");
        t.setEmailAddresses(asList("add1", "add2"));
        queryElementRepository.save(t);
        QueryElement s = new QueryElement();
        s.setName("addressContains");
        s.setEmailAddresses(asList("email1", "email2"));
        queryElementRepository.save(s);
    }

    private void prepareDate(final Calendar calendar) {
        QueryElement t = new QueryElement();
        t.setName("today");
        t.setDate(calendar.getTime());
        queryElementRepository.save(t);
        QueryElement y = new QueryElement();
        calendar.add(Calendar.DATE, -1);
        y.setName("yesterday");
        y.setDate(calendar.getTime());
        queryElementRepository.save(y);
        QueryElement to = new QueryElement();
        calendar.add(Calendar.DATE, 2);
        to.setName("tomorrow");
        to.setDate(calendar.getTime());
        queryElementRepository.save(to);
        calendar.add(Calendar.DATE, -1);
    }

    @Test
    public void should_query_element() {
        ChildrenElement c1 = new ChildrenElement();
        c1.setChildName("c1");
        c1.setParentName("p1");
        ChildrenElement c2 = new ChildrenElement();
        c2.setChildName("c2");
        c2.setParentName("p2");
        childrenRepository.saveAll(asList(c1, c2));

        // Test query
        List<ChildrenElement> queryAll = childrenRepository.getAllElementObject();
        assertThat(queryAll.size(), is(2));

        // Test query with parameters
        Optional<ChildrenElement> byName = childrenRepository.findByName("c1");
        assertThat(byName.isPresent(), is(true));
        assertThat(byName.get().getChildName(), is("c1"));
        assertThat(byName.get().getParentName(), is("p1"));

        // Test query with projection
        List<ProjectionObject> projectionObjects = childrenRepository.getAllAsProjection();
        assertThat(projectionObjects.size(), is(2));
        assertThat(projectionObjects.get(0).getCName(), is("c1"));
        assertThat(projectionObjects.get(1).getCName(), is("c2"));
        assertThat(projectionObjects.get(0).getPName(), is("p1"));
        assertThat(projectionObjects.get(1).getPName(), is("p2"));
    }

    @Test
    public void should_return_pageable_result() {
        IntStream.range(0, 20).forEach(i -> {
            ChildrenElement c = new ChildrenElement();
            c.setChildName("c" + i);
            c.setParentName("p" + i / 10);
            childrenRepository.save(c);
        });

        Page<ProjectionObject> page = childrenRepository.getAsProjectionWithParentName("p1",
                PageRequest.of(2, 3, Sort.by("childName")));
        assertThat(page.getTotalElements(), is(10L));
        List<ProjectionObject> content = page.getContent();
        assertThat(content.size(), is(3));
        assertThat(content.get(0).getCName(), is("c16"));
        assertThat(content.get(1).getCName(), is("c17"));
        assertThat(content.get(2).getCName(), is("c18"));
    }

    @Test
    public void should_return_between_record() {
        IntStream.range(0, 20).forEach(i -> {
            ChildrenElement c = new ChildrenElement();
            c.setChildName("c" + i);
            c.setParentName("p" + i / 10);
            childrenRepository.save(c);
        });
        List<ChildrenProjection> lp = childrenRepository.findByChildNameBetween("c1", "c4");
        assertThat(lp.size(), is(14));
        List<String> cNames = lp.stream().map(ChildrenProjection::getChildName).collect(Collectors.toList());
        assertThat(cNames, hasItems("c1", "c2", "c3", "c4", "c10", "c11", "c12", "c13", "c14", "c15", "c16",
                "c17", "c18", "c19"));

        List<ChildrenProjection> lc = childrenRepository.findByChildNameIn(asList("c2", "c3", "c5"));
        assertThat(lc.size(), is(3));

        List<ChildrenProjection> ls = childrenRepository.findByChildNameStartingWith("c");
        assertThat(ls.size(), is(20));

        List<ChildrenProjection> le = childrenRepository.findByChildNameEndingWith("3");
        assertThat(le.size(), is(2));

        List<ChildrenProjection> lee = childrenRepository.findByChildNameEndingWith("13");
        assertThat(lee.size(), is(1));

        List<ChildrenProjection> lcc = childrenRepository.findByChildNameContaining("5");
        assertThat(lcc.size(), is(2));
    }

    private static final String DB_HOSTS = "plocal:orient-db/spring-data-query-test";

    @BeforeClass
    public static void initDB() {
        RepositoryTestBase.initDb(DB_HOSTS);
    }

    static class config extends RepositoryTestConfig {
        @Bean("orientdbConfig")
        public IOrientdbConfig dbConfig() {
            return orientdbConfig(DB_HOSTS);
        }
    }
}
