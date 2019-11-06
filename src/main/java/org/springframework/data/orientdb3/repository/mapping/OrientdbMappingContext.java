package org.springframework.data.orientdb3.repository.mapping;

import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

/**
 * {@link MappingContext} implementation.
 *
 * @author xxcxy
 */
public class OrientdbMappingContext
        extends AbstractMappingContext<OrientdbPersistentEntity<?>, OrientdbPersistentProperty> {

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.context.AbstractMappingContext#createPersistentEntity(org.springframework.data.util.TypeInformation)
     */
    @Override
    protected <T> OrientdbPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
        return new OrientdbPersistentEntity<>(typeInformation);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.context.AbstractMappingContext#createPersistentProperty(java.lang.reflect.Field, java.beans.PropertyDescriptor, org.springframework.data.mapping.model.MutablePersistentEntity, org.springframework.data.mapping.model.SimpleTypeHolder)
     */
    @Override
    protected OrientdbPersistentProperty createPersistentProperty(Property property, OrientdbPersistentEntity<?> owner,
                                                                  SimpleTypeHolder simpleTypeHolder) {
        return new OrientdbPersistentProperty(property, owner, simpleTypeHolder);
    }
}
