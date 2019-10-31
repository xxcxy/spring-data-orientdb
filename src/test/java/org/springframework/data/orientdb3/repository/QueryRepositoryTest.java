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
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
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

    @Test
    public void should_find_by_in() {
        prepareContaining();
        List<QueryElement> iq = queryElementRepository.findByNameIn(asList("nameContains", "addContains", "test"));
        assertThat(iq.size(), is(1));
        assertThat(iq.get(0).getName(), is("nameContains"));
    }

    @Test
    public void should_find_by_between() {
        prepareBetween();
        List<QueryElement> bq = queryElementRepository.findByScoreBetween(1.0, 9.9);
        assertThat(bq.size(), is(1));
        assertThat(bq.get(0).getName(), is("middle"));
    }

    @Test
    public void should_find_by_starting_with() {
        prepareBetween();
        List<QueryElement> sq = queryElementRepository.findByNameStartingWith("mi");
        assertThat(sq.size(), is(2));
    }

    @Test
    public void should_find_by_ending_with() {
        prepareBetween();
        List<QueryElement> eq = queryElementRepository.findByNameEndingWith("le");
        assertThat(eq.size(), is(1));
        assertThat(eq.get(0).getName(), is("middle"));
    }

    @Test
    public void should_find_by_exists() {
        prepareExists();
        List<QueryElement> eq = queryElementRepository.findByNameExists();
        assertThat(eq.size(), is(1));
        assertThat(eq.get(0).getName(), is("exists"));
    }

    @Test
    public void should_find_by_true() {
        prepareBoolean();
        List<QueryElement> tq = queryElementRepository.findByActivatedIsTrue();
        assertThat(tq.size(), is(1));
        assertThat(tq.get(0).getName(), is("activated"));
    }

    @Test
    public void should_find_by_false() {
        prepareBoolean();
        List<QueryElement> fq = queryElementRepository.findByActivatedIsFalse();
        assertThat(fq.size(), is(1));
        assertThat(fq.get(0).getName(), is("inactivated"));
    }

    @Test
    public void should_find_by_is() {
        prepareBoolean();
        List<QueryElement> iq = queryElementRepository.findByNameIs("activated");
        assertThat(iq.size(), is(1));
        assertThat(iq.get(0).getName(), is("activated"));
    }

    @Test
    public void should_find_by_not_null() {
        prepareExists();
        List<QueryElement> eq = queryElementRepository.findByNameNotNull();
        assertThat(eq.size(), is(1));
        assertThat(eq.get(0).getName(), is("exists"));
    }

    @Test
    public void should_find_by_null() {
        prepareExists();
        List<QueryElement> nq = queryElementRepository.findByNameNull();
        assertThat(nq.size(), is(1));
        assertThat(nq.get(0).getScore(), is(20.0));
        assertThat(nq.get(0).getName(), nullValue());
    }

    @Test
    public void should_find_by_greater_than() {
        prepareBetween();
        List<QueryElement> gq = queryElementRepository.findByScoreGreaterThan(5.0);
        assertThat(gq.size(), is(1));
        assertThat(gq.get(0).getName(), is("max"));
    }

    @Test
    public void should_find_by_greater_than_equal() {
        prepareBetween();
        List<QueryElement> gq = queryElementRepository.findByScoreGreaterThanEqual(5.0);
        assertThat(gq.size(), is(2));
    }

    @Test
    public void should_find_by_less_than() {
        prepareBetween();
        List<QueryElement> lq = queryElementRepository.findByScoreLessThan(5.0);
        assertThat(lq.size(), is(1));
        assertThat(lq.get(0).getName(), is("min"));
    }

    @Test
    public void should_find_by_less_than_equal() {
        prepareBetween();
        List<QueryElement> lq = queryElementRepository.findByScoreLessThanEqual(5.0);
        assertThat(lq.size(), is(2));
    }

    @Test
    public void should_find_by_like() {
        prepareDate(Calendar.getInstance());
        List<QueryElement> lq = queryElementRepository.findByNameLike("%day");
        assertThat(lq.size(), is(2));
    }

    @Test
    public void should_find_by_not_like() {
        prepareDate(Calendar.getInstance());
        List<QueryElement> lq = queryElementRepository.findByNameNotLike("%day");
        assertThat(lq.size(), is(1));
        assertThat(lq.get(0).getName(), is("tomorrow"));
    }

    @Test
    public void should_find_by_regex() {
        prepareDate(Calendar.getInstance());
        List<QueryElement> rq = queryElementRepository.findByNameRegex(".*ter.*");
        assertThat(rq.size(), is(1));
        assertThat(rq.get(0).getName(), is("yesterday"));
    }

    @Test
    public void should_find_by_and_condition() {
        prepareAndOr();
        List<QueryElement> tt = queryElementRepository.findByNameAndDescription("tName", "tDesc");
        assertThat(tt.size(), is(1));
        assertThat(tt.get(0).getName(), is("tName"));
        assertThat(tt.get(0).getDescription(), is("tDesc"));
    }

    @Test
    public void should_find_by_or_condition() {
        prepareAndOr();
        List<QueryElement> tq = queryElementRepository.findByNameOrDescription("tName", "qDesc");
        assertThat(tq.size(), is(3));
    }

    @Test
    public void should_find_entity() {
        QueryElement t = new QueryElement();
        t.setName("name");
        queryElementRepository.save(t);
        QueryElement f = queryElementRepository.findEntityByName("name");
        assertThat(f.getName(), is("name"));
    }

    @Test
    public void should_find_optional() {
        QueryElement t = new QueryElement();
        t.setName("name");
        queryElementRepository.save(t);
        Optional<QueryElement> f = queryElementRepository.findOptionByName("name");
        assertThat(f.isPresent(), is(true));
        assertThat(f.get().getName(), is("name"));
    }

    @Test
    public void should_find_page() {
        prepareListData();
        Page<QueryElement> p = queryElementRepository.findByName("name", PageRequest.of(2, 5));
        assertThat(p.getTotalElements(), is(20L));
        assertThat(p.getContent().size(), is(5));
    }

    @Test
    public void should_find_iterable() {
        prepareListData();
        Iterable<QueryElement> i = queryElementRepository.findIterableByName("name");
        int count = 0;
        for (QueryElement e : i) {
            assertThat(e.getName(), is("name"));
            count++;
        }
        assertThat(count, is(20));
    }

    @Test
    public void should_find_stream() {
        prepareListData();
        Stream<QueryElement> s = queryElementRepository.findStreamByName("name");
        List<QueryElement> l = s.collect(Collectors.toList());
        assertThat(l.size(), is(20));
    }

    private void prepareListData() {
        queryElementRepository.saveAll(IntStream.range(0, 20).mapToObj(i -> {
            QueryElement q = new QueryElement();
            q.setName("name");
            q.setDescription("desc" + i);
            return q;
        }).collect(toList()));
    }

    private void prepareAndOr() {
        QueryElement tt = new QueryElement();
        tt.setName("tName");
        tt.setDescription("tDesc");
        queryElementRepository.save(tt);
        QueryElement tq = new QueryElement();
        tq.setName("tName");
        tq.setDescription("qDesc");
        queryElementRepository.save(tq);
        QueryElement qt = new QueryElement();
        qt.setName("qName");
        qt.setDescription("tDesc");
        queryElementRepository.save(qt);
        QueryElement qq = new QueryElement();
        qq.setName("qName");
        qq.setDescription("qDesc");
        queryElementRepository.save(qq);
    }

    private void prepareBoolean() {
        QueryElement t = new QueryElement();
        t.setName("activated");
        t.setActivated(true);
        queryElementRepository.save(t);
        QueryElement f = new QueryElement();
        f.setName("inactivated");
        f.setActivated(false);
        queryElementRepository.save(f);
    }

    private void prepareExists() {
        QueryElement e = new QueryElement();
        e.setName("exists");
        e.setScore(10.0);
        queryElementRepository.save(e);
        QueryElement n = new QueryElement();
        n.setScore(20.0);
        queryElementRepository.save(n);
    }

    private void prepareBetween() {
        QueryElement f = new QueryElement();
        f.setName("min");
        f.setScore(0.3);
        queryElementRepository.save(f);
        QueryElement s = new QueryElement();
        s.setName("middle");
        s.setScore(5.0);
        queryElementRepository.save(s);
        QueryElement t = new QueryElement();
        t.setName("max");
        t.setScore(10.0);
        queryElementRepository.save(t);
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
        List<String> cNames = lp.stream().map(ChildrenProjection::getChildName).collect(toList());
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
