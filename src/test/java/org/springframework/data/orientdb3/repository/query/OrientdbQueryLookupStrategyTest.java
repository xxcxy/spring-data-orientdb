package org.springframework.data.orientdb3.repository.query;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.orientdb3.repository.Query;
import org.springframework.data.orientdb3.repository.support.OrientdbIdParserHolder;
import org.springframework.data.orientdb3.repository.support.StringIdParser;
import org.springframework.data.orientdb3.support.OrientdbEntityManager;
import org.springframework.data.orientdb3.test.sample.SimpleElement;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrientdbQueryLookupStrategyTest {

    @Mock
    private OrientdbEntityManager em;
    @Mock
    private NamedQueries namedQueries;
    @Mock
    private ProjectionFactory projectionFactory;
    @Mock
    private OrientdbIdParserHolder orientdbIdParserHolder;

    @Test
    public void should_return_declaredQuery_when_annotated_query() throws Exception {
        when(orientdbIdParserHolder.getIdParser(String.class)).thenReturn(Optional.of(new StringIdParser()));
        QueryLookupStrategy strategy = OrientdbQueryLookupStrategy.create(em,
                QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND, QueryMethodEvaluationContextProvider.DEFAULT,
                orientdbIdParserHolder);
        Method method = UserRepository.class.getMethod("findByFoo", String.class);
        RepositoryMetadata metadata = new DefaultRepositoryMetadata(UserRepository.class);

        RepositoryQuery query = strategy.resolveQuery(method, metadata, projectionFactory, namedQueries);
        assertThat(query, IsInstanceOf.instanceOf(DeclaredQuery.class));
    }

    interface UserRepository extends Repository<SimpleElement, String> {
        @Query("something absurd")
        SimpleElement findByFoo(String foo);
    }
}
