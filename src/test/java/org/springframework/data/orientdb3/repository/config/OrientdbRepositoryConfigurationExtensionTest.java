package org.springframework.data.orientdb3.repository.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class OrientdbRepositoryConfigurationExtensionTest {

    @Mock
    RepositoryConfigurationSource configSource;

    @Test
    public void should_register_beanPostProcessors() {

        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

        RepositoryConfigurationExtension extension = new OrientdbRepositoryConfigurationExtension();
        extension.registerBeansForRoot(factory, configSource);

        Iterable<String> names = Arrays.asList(factory.getBeanDefinitionNames());

        assertThat(names, hasItems("collectOrientdbIdParserPostProcessor", "schemaGenerationPostProcessor"));
    }

    @Test
    public void should_register_sessionFactory() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

        RepositoryConfigurationExtension extension = new OrientdbRepositoryConfigurationExtension();
        extension.registerBeansForRoot(factory, configSource);

        Iterable<String> names = Arrays.asList(factory.getBeanDefinitionNames());

        assertThat(names, hasItems("sessionFactory"));
    }

    @Test
    public void should_register_parserHolder() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

        RepositoryConfigurationExtension extension = new OrientdbRepositoryConfigurationExtension();
        extension.registerBeansForRoot(factory, configSource);

        Iterable<String> names = Arrays.asList(factory.getBeanDefinitionNames());

        assertThat(names, hasItems("orientdbIdParserHolder"));
    }
}
