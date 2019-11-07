/*
 * Copyright 2012-2019 the original author or authors.
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
package org.springframework.data.orientdb3.repository.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.IdentifierAccessor;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.TargetAwareIdentifierAccessor;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.IdPropertyIdentifierAccessor;
import org.springframework.data.orientdb3.support.EntityProxyInterface;
import org.springframework.data.util.TypeInformation;

import java.util.Comparator;

/**
 * Orientdb-specific entity.
 *
 * @author xxcxy
 */
class OrientdbPersistentEntity<T> extends BasicPersistentEntity<T, OrientdbPersistentProperty> {

    private static final Logger LOG = LoggerFactory.getLogger(OrientdbPersistentEntity.class);

    /**
     * Creates a new {@link OrientdbPersistentEntity} using the given {@link TypeInformation} and {@link Comparator}.
     *
     * @param information must not be {@literal null}.
     */
    public OrientdbPersistentEntity(TypeInformation<T> information) {

        super(information, null);

    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.BasicPersistentEntity#getIdentifierAccessor()
     */
    @Override
    public IdentifierAccessor getIdentifierAccessor(Object bean) {
        return new EntityProxyAwareIdentifierAccessor(this, bean);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.BasicPersistentEntity#returnPropertyIfBetterIdPropertyCandidateOrNull(org.springframework.data.mapping.PersistentProperty)
     */
    @Override
    protected OrientdbPersistentProperty returnPropertyIfBetterIdPropertyCandidateOrNull(OrientdbPersistentProperty
                                                                                                 property) {
        return property.isIdProperty() ? property : null;
    }

    /**
     * {@link IdentifierAccessor} that tries to use a {@link EntityProxyAwareIdentifierAccessor}
     * for id access to potentially avoid the initialization of Entity proxies. We're falling back to
     * the default behavior of {@link IdPropertyIdentifierAccessor} if that's not possible.
     *
     * @author xxcxy
     */
    private static class EntityProxyAwareIdentifierAccessor extends TargetAwareIdentifierAccessor {

        private final Object bean;
        private final OrientdbPersistentProperty idProperty;
        private final PersistentPropertyAccessor<?> accessor;

        /**
         * Creates a new {@link EntityProxyAwareIdentifierAccessor} for the given {@link OrientdbPersistentEntity}
         * and target bean.
         *
         * @param entity must not be {@literal null}.
         * @param bean   must not be {@literal null}.
         */
        EntityProxyAwareIdentifierAccessor(OrientdbPersistentEntity<?> entity, Object bean) {
            super(bean);
            this.bean = bean;
            this.idProperty = entity.getIdProperty();
            this.accessor = entity.getPropertyAccessor(bean);
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.mapping.IdentifierAccessor#getIdentifier()
         */
        @Override
        public Object getIdentifier() {
            if (bean instanceof EntityProxyInterface) {
                try {
                    return idProperty.getRequiredGetter().invoke(bean);
                } catch (Exception e) {
                    LOG.error("Id property must have getter.", e);
                }
            }
            if (idProperty != null) {
                return accessor.getProperty(this.idProperty);
            }
            return "temporaryId";
        }
    }
}
