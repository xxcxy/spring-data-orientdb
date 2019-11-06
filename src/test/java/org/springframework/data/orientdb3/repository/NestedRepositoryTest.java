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
import org.springframework.data.orientdb3.test.sample.ElementObject;
import org.springframework.data.orientdb3.test.sample.Pojo;
import org.springframework.data.orientdb3.test.sample.ProjectionNested;
import org.springframework.data.orientdb3.test.sample.ProjectionValue;
import org.springframework.data.orientdb3.test.sample.SimpleElement;
import org.springframework.data.orientdb3.test.sample.VertexObject;
import org.springframework.data.orientdb3.test.sample.VertexSource;
import org.springframework.data.orientdb3.test.sample.VertexTarget;
import org.springframework.data.orientdb3.test.sample.repository.ElementObjectRepository;
import org.springframework.data.orientdb3.test.sample.repository.VertexObjectRepository;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.data.orientdb3.test.sample.EnValue.SECOND;

@ContextConfiguration(classes = NestedRepositoryTest.config.class)
public class NestedRepositoryTest extends RepositoryTestBase {

    private VertexObjectRepository vertexRepository;
    private ElementObjectRepository elementRepository;

    @Before
    public void setup() {
        vertexRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(VertexObjectRepository.class);
        elementRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(ElementObjectRepository.class);
    }

    @Test
    public void should_return_with_nested_property_when_find_by() {
        VertexSource vertexSource = new VertexSource();
        vertexSource.setType("source");
        VertexTarget vertexTarget = new VertexTarget();
        vertexTarget.setType("target");
        VertexObject vertex = new VertexObject();
        vertex.setTarget(vertexTarget);
        vertex.setSource(vertexSource);
        vertex.setType("vertex");
        vertexRepository.save(vertex);

        List<VertexObject> find = vertexRepository.findByType("vertex");
        assertThat(find.size(), is(1));

        VertexObject findVertexObject = find.get(0);
        assertThat(findVertexObject.getSource().getType(), is("source"));
        assertThat(findVertexObject.getTarget().getType(), is("target"));
    }

    @Test
    public void should_return_with_nested_object() {
        ElementObject elementObject = new ElementObject();
        elementObject.setElementList(getListSimpleElement());
        elementObject.setElementMap(getMapSimpleElement());
        elementObject.setElementSet(getSetSimpleElement());
        elementObject.setLength(5);
        elementObject.setType("stringType");
        elementObject.setNames(Arrays.asList("name1", "name2"));
        elementObject.setMaps(getMap());
        elementObject.setSets(getSetString());
        Pojo pojo = new Pojo();
        pojo.setName("simple object");
        elementObject.setPojo(pojo);
        elementObject.setEnValue(SECOND);

        elementRepository.save(elementObject);

        ProjectionNested findObject = elementRepository.findByType("stringType").get(0);
        assertThat(findObject.getSize(), is(5L));
        assertThat(findObject.getType(), is("stringType"));
        assertThat(findObject.getEnValue(), is(SECOND));

        // Verify embedded property
        assertThat(findObject.getNames(), hasItems("name1", "name2"));
        assertThat(findObject.getSets(), hasItems("s1", "s2"));
        Map<String, Long> map = findObject.getMaps();
        assertThat(map.get("m1"), is(1L));
        assertThat(map.get("m2"), is(2L));

        // Verify link list
        List<ProjectionValue> elements = findObject.getElementList();
        assertThat(elements.size(), is(2));
        assertThat(elements.get(0).getValue(), is("list1"));
        assertThat(elements.get(1).getValue(), is("list2"));

        // Verify link set
        Set<ProjectionValue> elementSet = findObject.getElementSet();
        assertThat(elementSet.size(), is(2));

        // Verify link map
        Map<String, ProjectionValue> elementMap = findObject.getElementMap();
        assertThat(elementMap.size(), is(2));
        assertThat(elementMap.get("map1").getValue(), is("map1"));
        assertThat(elementMap.get("map2").getValue(), is("map2"));

        // Verify pojo
        assertThat(findObject.getPojo().getName(), is("simple object"));
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

    private static final String DB_HOSTS = "plocal:orient-db/spring-data-nested-test";

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
