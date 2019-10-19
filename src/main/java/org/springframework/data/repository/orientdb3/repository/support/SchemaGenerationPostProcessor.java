package org.springframework.data.repository.orientdb3.repository.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.repository.orientdb3.support.SessionFactory;

public class SchemaGenerationPostProcessor implements BeanFactoryPostProcessor {


    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory configurableListableBeanFactory)
            throws BeansException {
        // Generate schema
        SessionFactory sessionFactory = configurableListableBeanFactory.getBean(SessionFactory.class);
        sessionFactory.generateSchema();
    }


}
