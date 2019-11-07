/*
 * Copyright 2011-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.orientdb3.repository.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.orientdb3.repository.QueryResult;
import org.springframework.data.orientdb3.repository.mapping.OrientdbMappingContext;
import org.springframework.data.orientdb3.support.IOrientdbConfig;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link FactoryBean} to setup {@link OrientdbMappingContext} instances from Spring configuration.
 *
 * @author xxcxy
 */
public class OrientdbMappingContextFactoryBean extends AbstractFactoryBean<OrientdbMappingContext> {
    private static final Logger LOG = LoggerFactory.getLogger(OrientdbMappingContextFactoryBean.class);
    private String projectionScanPackage;

    @Autowired
    public void setOrientdbConfig(final IOrientdbConfig orientdbConfig) {
        projectionScanPackage = orientdbConfig.getProjectionScanPackage();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return OrientdbMappingContext.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
     */
    @Override
    protected OrientdbMappingContext createInstance() {
        OrientdbMappingContext context = new OrientdbMappingContext();
        if (!StringUtils.isEmpty(projectionScanPackage)) {
            context.setInitialEntitySet(getClasses(projectionScanPackage));
        }
        context.initialize();
        return context;
    }

    /**
     * Scans a package and find all specified classes.
     *
     * @param scanPackage
     * @return
     */
    private Set<? extends Class<?>> getClasses(final String scanPackage) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(QueryResult.class));
        Set<BeanDefinition> beanDefinitionSet = provider.findCandidateComponents(scanPackage);
        Set<Class<?>> entityClasses = new HashSet<>();
        for (BeanDefinition beanDefinition : beanDefinitionSet) {
            String beanClassName = beanDefinition.getBeanClassName();
            try {
                entityClasses.add(Class.forName(beanClassName));
            } catch (ClassNotFoundException e) {
                LOG.error("Create class: {}'s persistent info error: ", beanClassName, e);
            }
        }
        return entityClasses;
    }
}
