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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.data.orientdb3.repository.mapping.OrientdbMappingContext;

/**
 * {@link FactoryBean} to setup {@link OrientdbMappingContext} instances from Spring configuration.
 *
 * @author Mark Angrish
 * @author Nicolas Mervaillie
 * @author Michael J. Simons
 */
public class OrientdbMappingContextFactoryBean extends AbstractFactoryBean<OrientdbMappingContext> {

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
        context.initialize();
        return context;
    }

}
