package org.springframework.data.repository.orientdb3.repository.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.repository.orientdb3.repository.EdgeEntity;
import org.springframework.data.repository.orientdb3.repository.ElementEntity;
import org.springframework.data.repository.orientdb3.repository.VertexEntity;
import org.springframework.data.repository.orientdb3.support.SessionFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class SchemaGenerationPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaGenerationPostProcessor.class);

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory configurableListableBeanFactory)
            throws BeansException {
        List<Class> entityClasses = new ArrayList<>();
        entityClasses.addAll(getClasses(ElementEntity.class, configurableListableBeanFactory));
        entityClasses.addAll(getClasses(VertexEntity.class, configurableListableBeanFactory));
        entityClasses.addAll(getClasses(EdgeEntity.class, configurableListableBeanFactory));

        // Generate schema
        SessionFactory sessionFactory = configurableListableBeanFactory.getBean(SessionFactory.class);
        sessionFactory.generateSchema(entityClasses);
    }

    private List<Class> getClasses(final Class<? extends Annotation> annotationClass,
                                   final ConfigurableListableBeanFactory configurableListableBeanFactory) {
        List<Class> entityClasses = new ArrayList<>();
        for (String beanName : configurableListableBeanFactory.getBeanNamesForAnnotation(annotationClass)) {
            String beanClassName = configurableListableBeanFactory.getBeanDefinition(beanName).getBeanClassName();
            try {
                entityClasses.add(Class.forName(beanClassName));
            } catch (ClassNotFoundException e) {
                LOGGER.error("Generate class: {}'s schema error: ", beanClassName, e);
            }
        }
        return entityClasses;
    }
}
