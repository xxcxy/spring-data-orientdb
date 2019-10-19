package org.springframework.data.repository.orientdb3.repository;

import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.orientdb3.repository.config.EnableOrientdbRepositories;
import org.springframework.data.repository.orientdb3.repository.support.OrientdbIdParserHolder;
import org.springframework.data.repository.orientdb3.repository.support.OrientdbRepositoryFactory;
import org.springframework.data.repository.orientdb3.repository.support.StringIdParser;
import org.springframework.data.repository.orientdb3.support.IOrientdbConfig;
import org.springframework.data.repository.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.repository.orientdb3.support.SessionFactory;
import org.springframework.data.repository.orientdb3.test.sample.EdgeObject;
import org.springframework.data.repository.orientdb3.test.sample.ElementObject;
import org.springframework.data.repository.orientdb3.test.sample.SimpleElement;
import org.springframework.data.repository.orientdb3.test.sample.VertexObject;
import org.springframework.data.repository.orientdb3.test.sample.repository.ElementObjectRepository;
import org.springframework.data.repository.orientdb3.transaction.OrientdbTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RepositoryTestConfig.class)
@Transactional
public class OrientdbRepositoryTest {

    @Autowired
    private SessionFactory sessionFactory;

    private OrientdbRepository<ElementObject, String> elementRepository;
    private OrientdbRepository<VertexObject, String> vertexRepository;
    private OrientdbRepository<EdgeObject, String> edgeRepository;

    @Before
    public void setup() {
        elementRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(ElementObjectRepository.class);
    }

    @Test
    public void should_insert_element_and_find() {
        ElementObject elementObject = new ElementObject();
        elementObject.setElementList(getListSimpleElement());
        elementObject.setElementMap(getMapSimpleElement());
        elementObject.setElementSet(getSetSimpleElement());
        elementObject.setLength(5);
        elementObject.setType("stringType");
        elementObject.setNames(Arrays.asList("name1", "name2"));
        elementObject.setMaps(getMap());
        elementObject.setSets(getSetString());

        elementRepository.save(elementObject);

        ElementObject findObject = elementRepository.findById(elementObject.getId()).get();
        assertThat(findObject.getLength(), is(5L));
        assertThat(findObject.getType(), is("stringType"));

        // Verify embedded property
        assertThat(findObject.getNames(), hasItems("name1", "name2"));
        assertThat(findObject.getSets(), hasItems("s1", "s2"));
        Map<String, Long> map = findObject.getMaps();
        assertThat(map.get("m1"), is(1L));
        assertThat(map.get("m2"), is(2L));

        // Verify link list
        List<SimpleElement> elements = findObject.getElementList();
        assertThat(elements.size(), is(2));
        assertThat(elements.get(0).getValue(), is("list1"));
        assertThat(elements.get(1).getValue(), is("list2"));

        // Verify link set
        Set<SimpleElement> elementSet = findObject.getElementSet();
        assertThat(elementSet.size(), is(2));

        // Verify link map
        Map<String, SimpleElement> elementMap = findObject.getElementMap();
        assertThat(elementMap.size(), is(2));
        assertThat(elementMap.get("map1").getValue(), is("map1"));
        assertThat(elementMap.get("map2").getValue(), is("map2"));
    }

    private Set<String> getSetString() {
        Set<String> sets = new HashSet<>();
        sets.add("s1");
        sets.add("s2");
        return sets;
    }

    private Map<String, Long> getMap() {
        Map<String, Long> map = new HashMap<>();
        map.put("m1", 1L);
        map.put("m2", 2L);
        return map;
    }

    private List<SimpleElement> getListSimpleElement() {
        SimpleElement se1 = new SimpleElement();
        se1.setValue("list1");
        SimpleElement se2 = new SimpleElement();
        se2.setValue("list2");
        return Arrays.asList(se1, se2);
    }

    private Set<SimpleElement> getSetSimpleElement() {
        SimpleElement se1 = new SimpleElement();
        se1.setValue("set1");
        SimpleElement se2 = new SimpleElement();
        se2.setValue("set2");
        Set<SimpleElement> sets = new HashSet<>();
        sets.add(se1);
        sets.add(se2);
        return sets;
    }

    private Map<String, SimpleElement> getMapSimpleElement() {
        SimpleElement se1 = new SimpleElement();
        se1.setValue("map1");
        SimpleElement se2 = new SimpleElement();
        se2.setValue("map2");
        Map<String, SimpleElement> maps = new HashMap<>();
        maps.put("map1", se1);
        maps.put("map2", se2);
        return maps;
    }

    @BeforeClass
    public static void initDb() {
        OrientDB orientDB = new OrientDB("plocal:orient-db/spring-data-test", OrientDBConfig.defaultConfig());
        if (orientDB.exists("repository_test")) {
            orientDB.drop("repository_test");
        }
        orientDB.create("repository_test", ODatabaseType.PLOCAL);
        orientDB.close();
    }

//    @AfterClass
//    public static void teardown() {
//        OrientDB orientDB = new OrientDB("plocal:orient-db/spring-data-test", OrientDBConfig.defaultConfig());
//        orientDB.drop("repository_test");
//        orientDB.close();
//    }

}

@Configuration
@EnableTransactionManagement
@EnableOrientdbRepositories
class RepositoryTestConfig {
    @Bean("orientdbConfig")
    public IOrientdbConfig orientdbConfig() {
        return new IOrientdbConfig() {
            @Override
            public String getUrl() {
                return "plocal:orient-db/spring-data-test";
            }

            @Override
            public String getServerUser() {
                return null;
            }

            @Override
            public String getServerPassword() {
                return null;
            }

            @Override
            public String getDatabase() {
                return "repository_test";
            }

            @Override
            public String getUserName() {
                return "admin";
            }

            @Override
            public boolean getAutoGenerateSchema() {
                return true;
            }

            @Override
            public String getEntityScanPackage() {
                return "org.springframework.data.repository.orientdb3";
            }

            @Override
            public String getPassword() {
                return "admin";
            }
        };
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new OrientdbTransactionManager(sessionFactory);
    }
}
