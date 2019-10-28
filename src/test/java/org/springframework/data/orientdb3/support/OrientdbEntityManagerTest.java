package org.springframework.data.orientdb3.support;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.OElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
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

    private final static ORID record = mock(ORID.class);
    private final static OElement element = mock(OElement.class);

    @Before
    public void setup() {
        entityManager = new OrientdbEntityManager(sessionFactory);
        when(session.countClass("countClass")).thenReturn(20L);
        ORecordIteratorClass oi = mock(ORecordIteratorClass.class);
        when(oi.hasNext()).thenReturn(true, true, true, false);
        when(oi.next()).thenReturn(mock(OElement.class), mock(OElement.class), mock(OElement.class));
        when(session.browseClass("findAll")).thenReturn(oi);
        when(session.load(record)).thenReturn(element);

        when(sessionFactory.openSession()).thenReturn(session);
    }

    @Test
    public void should_persist_entity() {
        Object obj = new Object();

        entityManager.persist(obj, entityInformation);

        verify(entityInformation).save(obj, session, null, new HashMap<>());
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

        when(entityInformation.convertToORID("id")).thenReturn(record);
        when(entityInformation.getEntityProxy(element, new HashMap<>())).thenReturn(obj);
        Object find = entityManager.find("id", entityInformation);

        assertThat(find, is(obj));
    }

    @Test
    public void should_find_all() {
        when(entityInformation.getEntityName()).thenReturn("findAll");

        List list = entityManager.findAll(entityInformation);
        assertThat(list.size(), is(3));
    }

    @Test
    public void should_count_class() {
        when(entityInformation.getEntityName()).thenReturn("countClass");

        assertThat(entityManager.count(entityInformation), is(20L));
    }
}
