package io.xxcxy.spring.data.orientdb.test.integration.repository;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import io.xxcxy.spring.data.orientdb.test.DomainObject;
import io.xxcxy.spring.data.orientdb.test.integration.RepositoryTestConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RepositoryTestConfiguration.class)
@Transactional
public class DomainObjectRepositoryTest {

    @BeforeClass
    public static void initDb() {
        OrientDB orientDB = new OrientDB("plocal:orient-db/spring-data-test", OrientDBConfig.defaultConfig());
        if (orientDB.createIfNotExists("test", ODatabaseType.PLOCAL)) {
            try (ODatabaseSession db = orientDB.open("test", "admin", "admin")) {
                db.newInstance("DomainObject");
            }
        }
        orientDB.close();
    }

    @Autowired
    private DomainObjectRepository repository;

    @Test
    public void should_find_domainObject() {
        repository.deleteAll();
        String firstProperty = "firstP1";
        String secondProperty = "secondP2";
        DomainObject domainObject = new DomainObject(firstProperty, secondProperty);
        repository.save(domainObject);
        List<DomainObject> domainObjects = repository.findAll();
        Assert.assertThat(domainObjects.size(), CoreMatchers.is(1));
        DomainObject dr = domainObjects.get(0);
        Assert.assertThat(dr.getP1(), CoreMatchers.is(firstProperty));
        Assert.assertThat(dr.getP2(), CoreMatchers.is(secondProperty));
    }
}
