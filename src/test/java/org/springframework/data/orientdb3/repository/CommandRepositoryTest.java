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
import org.springframework.data.orientdb3.test.sample.CommandElement;
import org.springframework.data.orientdb3.test.sample.repository.CommandElementRepository;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = CommandRepositoryTest.config.class)
public class CommandRepositoryTest extends RepositoryTestBase {

    private CommandElementRepository commandElementRepository;

    @Before
    public void setup() {
        commandElementRepository = new OrientdbRepositoryFactory(new OrientdbEntityManager(sessionFactory),
                new OrientdbIdParserHolder(new StringIdParser())).getRepository(CommandElementRepository.class);
    }

    @Test
    public void should_update_record() {
        prepareListData();
        commandElementRepository.updateNameByName("updated", "name10");

        Optional<CommandElement> optionalCommandElement = commandElementRepository.findByName("updated");
        assertThat(optionalCommandElement.isPresent(), is(true));
        assertThat(optionalCommandElement.get().getDescription(), is("desc10"));

        assertThat(commandElementRepository.findByName("name10").isPresent(), is(false));
    }

    @Test
    public void should_delete_record() {
        prepareListData();
        commandElementRepository.deleteByName("name2");

        assertThat(commandElementRepository.findAll().size(), is(19));
        assertThat(commandElementRepository.findByName("name2").isPresent(), is(false));
    }

    private void prepareListData() {
        commandElementRepository.saveAll(IntStream.range(0, 20).mapToObj(i -> {
            CommandElement q = new CommandElement();
            q.setName("name" + i);
            q.setDescription("desc" + i);
            return q;
        }).collect(toList()));
    }

    private static final String DB_HOSTS = "plocal:orient-db/spring-data-command-test";

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
