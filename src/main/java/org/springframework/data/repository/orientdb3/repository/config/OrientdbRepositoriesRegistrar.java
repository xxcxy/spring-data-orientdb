package org.springframework.data.repository.orientdb3.repository.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

public class OrientdbRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableOrientdbRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new OrientdbRepositoryConfigurationExtension();
    }
}
