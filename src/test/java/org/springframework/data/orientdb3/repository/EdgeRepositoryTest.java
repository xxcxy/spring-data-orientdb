package org.springframework.data.orientdb3.repository;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.data.orientdb3.repository.support.OrientdbIdParserHolder;
import org.springframework.data.orientdb3.repository.support.OrientdbRepositoryFactory;
import org.springframework.data.orientdb3.repository.support.StringIdParser;
import org.springframework.data.orientdb3.support.IOrientdbConfig;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.orientdb3.test.sample.VertexObject;
import org.springframework.data.orientdb3.test.sample.VertexSource;
import org.springframework.data.orientdb3.test.sample.VertexTarget;
import org.springframework.data.orientdb3.test.sample.repository.VertexObjectRepository;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = EdgeRepositoryTest.config.class)
public class EdgeRepositoryTest extends RepositoryTestBase {

    private OrientdbRepository<VertexObject, String> vertexRepository;

    @Before
    public void setup() {
        vertexRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(VertexObjectRepository.class);
    }

    @Test
    public void should_have_in_and_out_edges() {
        VertexSource vertexSource = new VertexSource();
        vertexSource.setType("source");
        VertexTarget vertexTarget = new VertexTarget();
        vertexTarget.setType("target");
        VertexObject vertex = new VertexObject();
        vertex.setTarget(vertexTarget);
        vertex.setSource(vertexSource);
        vertex.setType("vertex");
        vertexRepository.save(vertex);

        boolean hasTestIn = false;
        boolean hasTestOut = false;
        ODatabaseSession session = getSession();
        for (ODocument o : session.browseClass("TestIncoming")) {
            o.asEdge().ifPresent(e -> {
                assertThat(e.getTo().getProperty("type"), is("vertex"));
                assertThat(e.getFrom().getProperty("type"), is("source"));
            });
            hasTestIn = o.asEdge().isPresent();
        }

        for (ODocument o : session.browseClass("TestOutgoing")) {
            o.asEdge().ifPresent(e -> {
                assertThat(e.getTo().getProperty("type"), is("target"));
                assertThat(e.getFrom().getProperty("type"), is("vertex"));
            });
            hasTestOut = o.asEdge().isPresent();
        }

        assertThat(hasTestIn, is(true));
        assertThat(hasTestOut, is(true));
    }

    private static final String DB_HOSTS = "plocal:orient-db/spring-data-edge-test";

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
