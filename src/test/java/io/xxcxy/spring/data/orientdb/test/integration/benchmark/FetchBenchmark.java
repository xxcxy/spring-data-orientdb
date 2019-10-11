package io.xxcxy.spring.data.orientdb.test.integration.benchmark;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import io.xxcxy.spring.data.orientdb.support.SessionFactory;
import io.xxcxy.spring.data.orientdb.test.integration.BenchmarkFetchTestConfiguration;
import io.xxcxy.spring.data.orientdb.test.integration.repository.BenchmarkObjectRepository;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BenchmarkFetchTestConfiguration.class)
public class FetchBenchmark extends AbstractBenchmark {

    private static BenchmarkObjectRepository repository;
    private static SessionFactory sessionFactory;

    @Autowired
    void setRepository(BenchmarkObjectRepository repository) {
        FetchBenchmark.repository = repository;
    }

    @Autowired
    void setSessionFactory(SessionFactory sessionFactory) {
        FetchBenchmark.sessionFactory = sessionFactory;
    }

    @BeforeClass
    public static void initDb() {
        OrientDB orientDB = new OrientDB("plocal:orient-db/spring-data-test", OrientDBConfig.defaultConfig());
        if (orientDB.createIfNotExists("benchmarkfetch", ODatabaseType.PLOCAL)) {
            try (ODatabaseSession session = orientDB.open("benchmarkfetch", "admin", "admin")) {
                session.newInstance("BenchmarkObject");
                IntStream.range(0, 10000).forEach(i -> {
                    ODocument oDocument = new ODocument("BenchmarkObject");
                    oDocument.setProperty("name", "number" + i, OType.STRING);
                    oDocument.setProperty("email", "email@number" + i, OType.STRING);
                    session.save(oDocument);
                });
            }
        }
        orientDB.close();
    }

    @Benchmark
    public void insertWithRepository() {
        repository.findAll();
    }

    @Benchmark
    public void insertWithMultiModelApi() {
        List<ODocument> documents = new ArrayList<>();
        ODatabaseSession session = sessionFactory.openSession();
        for (ODocument oDocument : session.browseClass("BenchmarkObject")) {
            documents.add(oDocument);
        }
        session.close();
    }
}
