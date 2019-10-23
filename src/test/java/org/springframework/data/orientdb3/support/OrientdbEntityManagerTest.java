package org.springframework.data.orientdb3.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.OElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.orientdb3.repository.query.TypedQuery;
import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class OrientdbEntityManagerTest {

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private OrientdbEntityInformation<Object, String> entityInformation;
    @Mock
    private ODatabaseSession session;

    private OrientdbEntityManager entityManager;

    @Before
    public void setup() {
        entityManager = new OrientdbEntityManager(sessionFactory);
        when(sessionFactory.openSession()).thenReturn(session);
    }

    @Test
    public void should_persist_entity() {
        Object obj = new Object();
        OElement record = mock(OElement.class);
        when(entityInformation.convertToORecord(obj, session)).thenReturn(record);

        entityManager.persist(obj, entityInformation);

        verify(session).save(record);
        verify(entityInformation).setId(obj, null);
    }

    @Test
    public void should_remove_entity() {
        EntityProxyInterface obj = mock(EntityProxyInterface.class);

        entityManager.remove(obj);

        verify(obj).deleteOElement();
    }

    @Test
    public void should_find_by_id() {
        Object obj = new Object();
        ORID record = mock(ORID.class);
        OElement element = mock(OElement.class);
        when(entityInformation.convertToORID("id")).thenReturn(record);
        when(session.load(record)).thenReturn(element);
        when(entityInformation.getEntityProxy(element)).thenReturn(obj);
        Object find = entityManager.find("id", entityInformation);

        assertThat(find, is(obj));
    }

    @Test
    public void should_find_all() {
        when(entityInformation.getEntityName()).thenReturn("entityName");
        ORecordIteratorClass oi = mock(ORecordIteratorClass.class);
        when(oi.hasNext()).thenReturn(true, true, true, false);
        when(oi.next()).thenReturn(mock(OElement.class), mock(OElement.class), mock(OElement.class));
        when(session.browseClass("entityName")).thenReturn(oi);

        List list = entityManager.findAll(entityInformation);
        assertThat(list.size(), is(3));
    }

    @Test
    public void should_count_class() {
        when(entityInformation.getEntityName()).thenReturn("entityName");
        when(session.countClass("entityName")).thenReturn(20L);

        assertThat(entityManager.count(entityInformation), is(20L));
    }

    @Test
    public void should_create_query() {
        when(entityInformation.getEntityName()).thenReturn("entityName");
        ArgumentCaptor<String> querySql = ArgumentCaptor.forClass(String.class);

        assertThat(entityManager.createQuery("select from %s", entityInformation), instanceOf(TypedQuery.class));
        verify(session).query(querySql.capture());

        assertThat(querySql.getValue(), is("select from entityName"));
    }
}
