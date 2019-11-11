package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StringIdParserTest {

    private StringIdParser parser = new StringIdParser();

    @Test
    public void should_return_id_class_is_string() {
        assertThat(parser.getIdClass().equals(String.class), is(true));
    }

    @Test
    public void should_parse_java_id() {
        ORID orid = parser.parseJavaId("1-5");
        assertThat(orid.getClusterId(), is(1));
        assertThat(orid.getClusterPosition(), is(5L));
    }

    @Test
    public void should_parse_orientdb_id() {
        String jId = parser.parseOrientdbId(new ORecordId("1-5"));
        assertThat(jId, is("1-5"));
    }
}
