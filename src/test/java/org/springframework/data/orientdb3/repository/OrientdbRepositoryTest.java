package org.springframework.data.orientdb3.repository;

import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.exception.OValidationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.orientdb3.repository.config.EnableOrientdbRepositories;
import org.springframework.data.orientdb3.repository.support.OrientdbIdParserHolder;
import org.springframework.data.orientdb3.repository.support.OrientdbRepositoryFactory;
import org.springframework.data.orientdb3.repository.support.StringIdParser;
import org.springframework.data.orientdb3.support.IOrientdbConfig;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.orientdb3.support.SessionFactory;
import org.springframework.data.orientdb3.test.sample.ChildrenElement;
import org.springframework.data.orientdb3.test.sample.EdgeObject;
import org.springframework.data.orientdb3.test.sample.ElementObject;
import org.springframework.data.orientdb3.test.sample.SimpleElement;
import org.springframework.data.orientdb3.test.sample.VertexObject;
import org.springframework.data.orientdb3.test.sample.VertexSource;
import org.springframework.data.orientdb3.test.sample.VertexTarget;
import org.springframework.data.orientdb3.test.sample.VertexWithEdges;
import org.springframework.data.orientdb3.test.sample.repository.ChildrenElementRepository;
import org.springframework.data.orientdb3.test.sample.repository.EdgeObjectRepository;
import org.springframework.data.orientdb3.test.sample.repository.ElementObjectRepository;
import org.springframework.data.orientdb3.test.sample.repository.VertexObjectRepository;
import org.springframework.data.orientdb3.test.sample.repository.VertexWithEdgesRepository;
import org.springframework.data.orientdb3.transaction.OrientdbTransactionManager;
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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RepositoryTestConfig.class)
@Transactional
public class OrientdbRepositoryTest {

    @Autowired
    private SessionFactory sessionFactory;

    private OrientdbRepository<ElementObject, String> elementRepository;
    private OrientdbRepository<ChildrenElement, String> childrenRepository;
    private OrientdbRepository<VertexObject, String> vertexRepository;
    private OrientdbRepository<EdgeObject, String> edgeRepository;

    @Before
    public void setup() {
        elementRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(ElementObjectRepository.class);
        childrenRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(ChildrenElementRepository.class);
        vertexRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(VertexObjectRepository.class);

        edgeRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(EdgeObjectRepository.class);
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

    @Test
    public void should_insert_vertex_and_find() {
        VertexTarget vertexTarget = new VertexTarget();
        vertexTarget.setType("target");
        VertexObject vertexObject = new VertexObject();
        vertexObject.setType("fromType");
        vertexObject.setTarget(vertexTarget);

        vertexRepository.save(vertexObject);
        VertexObject find = vertexRepository.findById(vertexObject.getId()).get();

        assertThat(find.getType(), is("fromType"));

        // Verify edge
        assertThat(find.getTarget().getType(), is("target"));
    }

    @Test
    public void should_insert_find_update_and_delete_vertex() {
        OrientdbRepository<VertexWithEdges, String> vertexWithEdgesRepository =
                new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                        new OrientdbIdParserHolder(new StringIdParser()))
                        .getRepository(VertexWithEdgesRepository.class);

        VertexWithEdges vertex = new VertexWithEdges();
        vertex.setType("withEdges");
        vertex.setTargets(Arrays.asList(new VertexTarget(), new VertexTarget(), new VertexTarget()));
        VertexSource vertexSource = new VertexSource();
        vertexSource.setType("source");
        vertex.setSource(vertexSource);

        // Test save and find
        vertexWithEdgesRepository.save(vertex);
        String vId = vertex.getId();
        VertexWithEdges find = vertexWithEdgesRepository.findById(vId).get();
        assertThat(find.getTargets().size(), is(3));
        assertThat(find.getType(), is("withEdges"));
        assertThat(find.getSource().getType(), is("source"));


        // Test update
        find.setType("updateEdges");
//        find.setSource(null);
//        List<VertexTarget> vl = find.getTargets();
//        vl.remove(1);
//        find.setTargets(vl);
        vertexWithEdgesRepository.save(find);
        VertexWithEdges updated = vertexWithEdgesRepository.findById(vId).get();
        assertThat(updated.getType(), is("updateEdges"));
//        assertThat(updated.getSource(), nullValue());
//        assertThat(updated.getTargets().size(), is(2));

        // Test delete
        vertexWithEdgesRepository.deleteById(vId);
        assertThat(vertexWithEdgesRepository.findById(vId).isPresent(), is(false));
    }

    @Test
    public void should_insert_edge_and_find() {
        VertexSource vertexSource = new VertexSource();
        vertexSource.setType("source");
        VertexTarget vertexTarget = new VertexTarget();
        vertexTarget.setType("target");
        EdgeObject edgeObject = new EdgeObject();
        edgeObject.setLength(15L);
        edgeObject.setSource(vertexSource);
        edgeObject.setTarget(vertexTarget);

        edgeRepository.save(edgeObject);
        EdgeObject find = edgeRepository.findById(edgeObject.getId()).get();

        assertThat(find.getLength(), is(15L));
        assertThat(find.getSource().getType(), is("source"));
        assertThat(find.getTarget().getType(), is("target"));
    }

    @Test
    public void should_update_edge_property() {
        VertexSource vertexSource = new VertexSource();
        vertexSource.setType("source");
        VertexTarget vertexTarget = new VertexTarget();
        vertexTarget.setType("target");
        EdgeObject edgeObject = new EdgeObject();
        edgeObject.setLength(15L);
        edgeObject.setType("first");
        edgeObject.setSource(vertexSource);
        edgeObject.setTarget(vertexTarget);

        edgeRepository.save(edgeObject);
        EdgeObject find = edgeRepository.findById(edgeObject.getId()).get();

        assertThat(find.getLength(), is(15L));
        assertThat(find.getType(), is("first"));

        find.setLength(5L);
        find.setType("second");

        edgeRepository.save(find);
        EdgeObject updated = edgeRepository.findById(edgeObject.getId()).get();

        assertThat(updated.getLength(), is(5L));
        assertThat(updated.getType(), is("second"));
    }

    @Test
    public void should_delete_edge() {
        VertexSource vertexSource = new VertexSource();
        vertexSource.setType("source");
        VertexTarget vertexTarget = new VertexTarget();
        vertexTarget.setType("target");
        EdgeObject edgeObject = new EdgeObject();
        edgeObject.setLength(15L);
        edgeObject.setType("first");
        edgeObject.setSource(vertexSource);
        edgeObject.setTarget(vertexTarget);

        edgeRepository.save(edgeObject);
        String edgeId = edgeObject.getId();
        EdgeObject find = edgeRepository.findById(edgeId).get();

        assertThat(find, notNullValue());

        edgeRepository.delete(find);

        assertThat(edgeRepository.findById(edgeId).isPresent(), is(false));
    }

    @Test(expected = OValidationException.class)
    public void should_insert_error_when_breach_of_constraint() {
        ElementObject elementObject = new ElementObject();
        elementObject.setElementList(getListSimpleElement());
        elementObject.setElementMap(getMapSimpleElement());
        elementObject.setElementSet(getSetSimpleElement());
        elementObject.setLength(15);
        elementObject.setType("stringType");
        elementObject.setNames(Arrays.asList("name1", "name2"));
        elementObject.setMaps(getMap());
        elementObject.setSets(getSetString());

        elementRepository.save(elementObject);
    }

    @Test
    public void should_delete_element() {
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

        String saveId = elementObject.getId();
        assertThat(elementRepository.findById(saveId).isPresent(), is(true));

        // Verify delete
        elementRepository.deleteById(saveId);
        assertThat(elementRepository.findById(saveId).isPresent(), is(false));
    }

    @Test
    public void should_be_inheritance() {
        ChildrenElement childrenElement = new ChildrenElement();
        childrenElement.setChildName("childName");
        childrenElement.setParentName("parentName");
        childrenRepository.save(childrenElement);

        ChildrenElement find = childrenRepository.findById(childrenElement.getId()).get();

        assertThat(find.getChildName(), is("childName"));
        // Verify parent property
        assertThat(find.getParentName(), is("parentName"));
    }

    @Test
    public void should_find_two_by_sort() {
        for (int i = 10; i > 0; i--) {
            ChildrenElement childrenElement = new ChildrenElement();
            childrenElement.setChildName(i + "childName");
            childrenElement.setParentName(i + "parentName");
            childrenRepository.save(childrenElement);
        }

        Page<ChildrenElement> childrenElements = childrenRepository.findAll(PageRequest.of(1, 3,
                Sort.Direction.ASC, "childName"));
        assertThat(childrenElements.getTotalElements(), is(10L));
        List<ChildrenElement> content = childrenElements.getContent();
        assertThat(content.size(), is(3));

        // 10child 1child 2child 3child so get(0) is 3childName
        assertThat(content.get(0).getChildName(), is("3childName"));
        assertThat(content.get(1).getChildName(), is("4childName"));
        assertThat(content.get(2).getChildName(), is("5childName"));
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
                return "org.springframework.data.orientdb3.test.sample";
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
