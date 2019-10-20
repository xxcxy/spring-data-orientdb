package org.springframework.data.repository.orientdb3.repository.config;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.data.repository.orientdb3.test.sample.repository.ElementObjectRepository;

import java.util.Arrays;

import static org.junit.Assert.assertThat;


public class OrientdbRepositoriesRegistrarTest {
    BeanDefinitionRegistry registry;
    AnnotationMetadata metadata;

    @Before
    public void setUp() {

        metadata = new StandardAnnotationMetadata(Config.class, true);
        registry = new DefaultListableBeanFactory();
    }

    @Test
    public void configuresRepositoriesCorrectly() {

        OrientdbRepositoriesRegistrar registrar = new OrientdbRepositoriesRegistrar();
        registrar.setResourceLoader(new DefaultResourceLoader());
        registrar.setEnvironment(new StandardEnvironment());
        registrar.registerBeanDefinitions(metadata, registry);

        Iterable<String> names = Arrays.asList(registry.getBeanDefinitionNames());
        assertThat(names, CoreMatchers.hasItems("childrenElementRepository", "edgeObjectRepository",
                "elementObjectRepository", "vertexObjectRepository"));
    }

    @EnableOrientdbRepositories(basePackageClasses = ElementObjectRepository.class)
    class Config {
    }

}
