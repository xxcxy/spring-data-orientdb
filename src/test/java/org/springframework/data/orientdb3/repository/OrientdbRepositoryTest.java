package org.springframework.data.orientdb3.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.core.exception.OValidationException;
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
import org.springframework.data.orientdb3.test.sample.Country;
import org.springframework.data.orientdb3.test.sample.EdgeObject;
import org.springframework.data.orientdb3.test.sample.ElementObject;
import org.springframework.data.orientdb3.test.sample.Pojo;
import org.springframework.data.orientdb3.test.sample.SimpleElement;
import org.springframework.data.orientdb3.test.sample.VertexInterrelatedOne;
import org.springframework.data.orientdb3.test.sample.VertexInterrelatedTwo;
import org.springframework.data.orientdb3.test.sample.VertexObject;
import org.springframework.data.orientdb3.test.sample.VertexSource;
import org.springframework.data.orientdb3.test.sample.VertexTarget;
import org.springframework.data.orientdb3.test.sample.VertexWithEdges;
import org.springframework.data.orientdb3.test.sample.repository.ChildrenElementRepository;
import org.springframework.data.orientdb3.test.sample.repository.CountryRepository;
import org.springframework.data.orientdb3.test.sample.repository.EdgeObjectRepository;
import org.springframework.data.orientdb3.test.sample.repository.ElementObjectRepository;
import org.springframework.data.orientdb3.test.sample.repository.VertexInterrelatedOneRepository;
import org.springframework.data.orientdb3.test.sample.repository.VertexObjectRepository;
import org.springframework.data.orientdb3.test.sample.repository.VertexSourceRepository;
import org.springframework.data.orientdb3.test.sample.repository.VertexWithEdgesRepository;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.springframework.data.orientdb3.test.sample.EnValue.FIRST;

@ContextConfiguration(classes = OrientdbRepositoryTest.config.class)
public class OrientdbRepositoryTest extends RepositoryTestBase {


    private OrientdbRepository<ElementObject, String> elementRepository;
    private OrientdbRepository<ChildrenElement, String> childrenRepository;
    private OrientdbRepository<VertexObject, String> vertexRepository;
    private OrientdbRepository<EdgeObject, String> edgeRepository;
    private OrientdbRepository<VertexWithEdges, String> vertexWithEdgesRepository;
    private OrientdbRepository<VertexInterrelatedOne, String> oneRepository;
    private OrientdbRepository<VertexSource, String> vertexSourceRepository;
    private OrientdbRepository<Country, String> countryRepository;

    @Before
    public void setup() {
        elementRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(ElementObjectRepository.class);
        childrenRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(ChildrenElementRepository.class);
        vertexRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(VertexObjectRepository.class);
        vertexWithEdgesRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser()))
                .getRepository(VertexWithEdgesRepository.class);
        edgeRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(EdgeObjectRepository.class);
        oneRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(VertexInterrelatedOneRepository.class);
        vertexSourceRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(VertexSourceRepository.class);
        countryRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(CountryRepository.class);
    }

    @Test
    public void should_use_custom_name() {
        Country country = new Country();
        country.setName("us");
        countryRepository.save(country);

        // Test custom name
        assertThat(getSession().browseClass("Countries").hasNext(), is(true));
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
        elementObject.setEnValue(FIRST);

        elementObject = elementRepository.save(elementObject);

        ElementObject findObject = elementRepository.findById(elementObject.getId()).get();
        assertThat(findObject.getLength(), is(5L));
        assertThat(findObject.getType(), is("stringType"));

        // Verify enum property
        assertThat(findObject.getEnValue(), is(FIRST));

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
    public void should_support_embedded_pojo() {
        Pojo pojo = new Pojo();
        pojo.setName("simple object");
        ElementObject elementObject = new ElementObject();
        elementObject.setLength(5);
        elementObject.setType("stringType");
        elementObject.setPojo(pojo);

        elementObject = elementRepository.save(elementObject);

        ElementObject find = elementRepository.findById(elementObject.getId()).get();

        assertThat(find.getPojo().getName(), is("simple object"));
    }

    @Test
    public void should_insert_vertex_and_find() {
        VertexTarget vertexTarget = new VertexTarget();
        vertexTarget.setType("target");
        VertexObject vertexObject = new VertexObject();
        vertexObject.setType("fromType");
        vertexObject.setTarget(vertexTarget);

        vertexObject = vertexRepository.save(vertexObject);
        VertexObject find = vertexRepository.findById(vertexObject.getId()).get();

        assertThat(find.getType(), is("fromType"));

        // Verify edge
        assertThat(find.getTarget().getType(), is("target"));
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

        edgeObject = edgeRepository.save(edgeObject);
        EdgeObject find = edgeRepository.findById(edgeObject.getId()).get();

        assertThat(find.getLength(), is(15L));
        assertThat(find.getSource().getType(), is("source"));
        assertThat(find.getTarget().getType(), is("target"));
    }

    @Test
    public void should_insert_find_update_and_delete_vertex() {
        VertexWithEdges vertex = new VertexWithEdges();
        vertex.setType("withEdges");
        vertex.setTargets(Arrays.asList(new VertexTarget(), new VertexTarget(), new VertexTarget()));
        VertexSource vertexSource = new VertexSource();
        vertexSource.setType("source");
        vertex.setSource(vertexSource);

        // Test save and find
        String vId = vertexWithEdgesRepository.save(vertex).getId();
        VertexWithEdges find = vertexWithEdgesRepository.findById(vId).get();
        assertThat(find.getType(), is("withEdges"));
        assertThat(find.getSource().getType(), is("source"));
        assertThat(find.getTargets().size(), is(3));

        // Test update
        find.setType("updateEdges");

        // Test #11
        find.getSource().setType("updatedSource");
        find.getTargets().add(new VertexTarget());
        vertexWithEdgesRepository.save(find);
        VertexWithEdges updated = vertexWithEdgesRepository.findById(vId).get();
        assertThat(updated.getType(), is("updateEdges"));

        // Verify #11
        assertThat(updated.getSource().getType(), is("updatedSource"));
        assertThat(updated.getTargets().size(), is(4));

        // Test delete
        vertexWithEdgesRepository.deleteById(vId);
        assertThat(vertexWithEdgesRepository.findById(vId).isPresent(), is(false));
    }

    @Test
    public void should_remove_edge_from_vertex() {
        VertexWithEdges vertex = new VertexWithEdges();
        vertex.setType("withEdges");
        vertex.setTargets(Arrays.asList(new VertexTarget(), new VertexTarget(), new VertexTarget()));
        VertexSource vertexSource = new VertexSource();
        vertexSource.setType("source");
        vertex.setSource(vertexSource);

        // Test save and find
        String vId = vertexWithEdgesRepository.save(vertex).getId();

        VertexWithEdges find = vertexWithEdgesRepository.findById(vId).get();
        assertThat(find.getTargets().size(), is(3));
        assertThat(find.getType(), is("withEdges"));
        assertThat(find.getSource().getType(), is("source"));

        // Test update and delete edge
        List<VertexTarget> lv = find.getTargets();
        lv.remove(0);
        find.setTargets(lv);
        VertexSource updateSource = new VertexSource();
        updateSource.setType("update");
        find.setSource(updateSource);

        vertexWithEdgesRepository.save(find);

        VertexWithEdges updated = vertexWithEdgesRepository.findById(vId).get();
        assertThat(updated.getTargets().size(), is(2));
        assertThat(updated.getSource().getType(), is("update"));
    }

    @Test
    public void should_save_and_find_interrelated_vertex() {
        VertexInterrelatedOne one = new VertexInterrelatedOne();
        VertexInterrelatedTwo two = new VertexInterrelatedTwo();
        one.setName("oneName");
        one.setTwo(two);
        two.setName("twoName");
        two.setOne(one);

        // Test save interrelated vertex
        one = oneRepository.save(one);
        assertThat(one.getName(), is("oneName"));
        assertThat(one.getTwo().getName(), is("twoName"));
        assertThat(one.getTwo().getOne().getName(), is("oneName"));

        assertSame("The one should be the same", one, one.getTwo().getOne());

        // Test find interrelated vertex
        VertexInterrelatedOne findOne = oneRepository.findById(one.getId()).get();
        assertThat(findOne.getName(), is("oneName"));
        assertThat(findOne.getTwo().getName(), is("twoName"));
        assertThat(findOne.getTwo().getOne().getName(), is("oneName"));

        assertSame("The one should be the same", findOne, findOne.getTwo().getOne());
    }

    @Test
    public void should_save_pojo_with_id_not_null() {
        VertexSource vs = new VertexSource();
        vs.setType("first");

        String vId = vertexSourceRepository.save(vs).getId();

        // Verify save success
        assertThat(vertexRepository.findById(vId).get().getType(), is("first"));

        VertexSource update = new VertexSource();
        update.setId(vId);
        update.setType("updated");
        vertexSourceRepository.save(update);

        assertThat(vertexRepository.findById(vId).get().getType(), is("updated"));
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

        String saveId = elementRepository.save(elementObject).getId();
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
        childrenElement = childrenRepository.save(childrenElement);

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

    @Test
    public void should_delete_all() {
        Country s1 = new Country();
        s1.setName("s1");
        Country s2 = new Country();
        s2.setName("s2");
        Country s3 = new Country();
        s3.setName("s3");
        countryRepository.saveAll(Arrays.asList(s1, s2, s3));

        assertThat(countryRepository.findAll().size(), is(3));

        // Test delete all
        countryRepository.deleteAll();
        assertThat(countryRepository.findAll().size(), is(0));
    }

    @Test
    public void should_generate_json() throws JsonProcessingException {
        ElementObject elementObject = new ElementObject();
        elementObject.setElementList(getListSimpleElement());
        elementObject.setElementMap(getMapSimpleElement());
        elementObject.setElementSet(getSetSimpleElement());
        elementObject.setLength(5);
        elementObject.setType("stringType");
        elementObject.setNames(Arrays.asList("name1", "name2"));
        elementObject.setMaps(getMap());
        elementObject.setSets(getSetString());

        elementObject = elementRepository.save(elementObject);

        ElementObject findObject = elementRepository.findById(elementObject.getId()).get();

        // Test convert to json
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(findObject);

        /*
        "{\"id\":\"#xx:xx\",\"length\":5,\"type\":\"stringType\"," +
                "\"names\":[\"name1\",\"name2\"],\"sets\":[\"s1\",\"s2\"],\"maps\":{\"m1\":1,\"m2\":2}," +
                "\"elementList\":[{\"id\":\"#xx:xx\",\"value\":\"list1\"},{\"id\":\"#xx:xx\",\"value\":\"list2\"}]," +
                "\"elementSet\":[{\"id\":\"#xx:xx\",\"value\":\"set2\"},{\"id\":\"#xx:xx\",\"value\":\"set1\"}]," +
                "\"elementMap\":{\"map2\":{\"id\":\"#xx:xx\",\"value\":\"map2\"}," +
                "\"map1\":{\"id\":\"#xx:xx\",\"value\":\"map1\"}},\"pojo\":null}"));
       **/

        assertThat(json, hasJsonPath("$.length", equalTo(5)));
        assertThat(json, hasJsonPath("$.type", equalTo("stringType")));
        assertThat(json, hasJsonPath("$.names", hasItems("name1", "name2")));
        assertThat(json, hasJsonPath("$.sets", hasItems("s1", "s2")));
        assertThat(json, hasJsonPath("$.maps.m1", equalTo(1)));
        assertThat(json, hasJsonPath("$.maps.m2", equalTo(2)));
        assertThat(json, hasJsonPath("$.elementList[0].value", equalTo("list1")));
        assertThat(json, hasJsonPath("$.elementList[1].value", equalTo("list2")));
        assertThat(json, hasJsonPath("$.elementSet", hasSize(2)));
        assertThat(json, hasJsonPath("$.elementMap.map2.value", equalTo("map2")));
        assertThat(json, hasJsonPath("$.elementMap.map1.value", equalTo("map1")));

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

    private static final String DB_HOSTS = "plocal:orient-db/spring-data-test";

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
