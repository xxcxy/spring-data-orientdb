package io.xxcxy.spring.data.orientdb.test.integration.benchmark;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import io.xxcxy.spring.data.orientdb.support.SessionFactory;
import io.xxcxy.spring.data.orientdb.test.BenchmarkObject;
import io.xxcxy.spring.data.orientdb.test.integration.BenchmarkInsertTestConfiguration;
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

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BenchmarkInsertTestConfiguration.class)
public class InsertBenchmark extends AbstractBenchmark {

    private static BenchmarkObjectRepository repository;
    private static SessionFactory sessionFactory;

    @Autowired
    void setRepository(BenchmarkObjectRepository repository) {
        InsertBenchmark.repository = repository;
    }

    @Autowired
    void setSessionFactory(SessionFactory sessionFactory) {
        InsertBenchmark.sessionFactory = sessionFactory;
    }

    @BeforeClass
    public static void initDb() {
        OrientDB orientDB = new OrientDB("plocal:orient-db/spring-data-test", OrientDBConfig.defaultConfig());
        if (orientDB.exists("benchmarkinsert")) {
            orientDB.drop("benchmarkinsert");
        }
        orientDB.create("benchmarkinsert", ODatabaseType.PLOCAL);
        try (ODatabaseSession session = orientDB.open("benchmarkinsert", "admin", "admin")) {
            session.newInstance("BenchmarkObject");
        }
        orientDB.close();
    }

    @Benchmark
    public void insertWithRepository() {
        repository.save(new BenchmarkObject("number", "email@number"));
    }

    @Benchmark
    public void insertWithMultiModelApi() {
        ODatabaseSession session = sessionFactory.openSession();
        session.begin();
        ODocument oDocument = new ODocument("BenchmarkObject");
        oDocument.setProperty("name", "number", OType.STRING);
        oDocument.setProperty("email", "email@number", OType.STRING);
        session.save(oDocument);
        session.commit();
        session.close();
    }
}
