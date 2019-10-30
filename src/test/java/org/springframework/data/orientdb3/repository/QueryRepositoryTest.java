package org.springframework.data.orientdb3.repository;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.data.orientdb3.repository.support.OrientdbIdParserHolder;
import org.springframework.data.orientdb3.repository.support.OrientdbRepositoryFactory;
import org.springframework.data.orientdb3.repository.support.StringIdParser;
import org.springframework.data.orientdb3.support.IOrientdbConfig;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.orientdb3.test.sample.ChildrenElement;
import org.springframework.data.orientdb3.test.sample.ProjectionObject;
import org.springframework.data.orientdb3.test.sample.repository.ChildrenElementRepository;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = QueryRepositoryTest.config.class)
public class QueryRepositoryTest extends RepositoryTestBase {

    private ChildrenElementRepository childrenRepository;

    @Before
    public void setup() {
        childrenRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(ChildrenElementRepository.class);
    }

    @Test
    public void should_query_element() {
        ChildrenElement c1 = new ChildrenElement();
        c1.setChildName("c1");
        c1.setParentName("p1");
        ChildrenElement c2 = new ChildrenElement();
        c2.setChildName("c2");
        c2.setParentName("p2");
        childrenRepository.saveAll(Arrays.asList(c1, c2));

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
