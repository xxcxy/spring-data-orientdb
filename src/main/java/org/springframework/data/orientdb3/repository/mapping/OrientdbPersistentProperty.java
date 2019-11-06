package org.springframework.data.orientdb3.repository.mapping;

import org.springframework.data.annotation.Version;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.orientdb3.repository.Edge;
import org.springframework.data.orientdb3.repository.Embedded;
import org.springframework.data.orientdb3.repository.FromVertex;
import org.springframework.data.orientdb3.repository.Link;
import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.repository.ToVertex;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Orientdb-specific {@link PersistentProperty}.
 *
 * @author xxcxy
 */
class OrientdbPersistentProperty extends AnnotationBasedPersistentProperty<OrientdbPersistentProperty> {

    private static final Collection<Class<? extends Annotation>> ASSOCIATION_ANNOTATIONS;
    private static final Collection<Class<? extends Annotation>> ID_ANNOTATIONS;

    static {

        Set<Class<? extends Annotation>> annotations = new HashSet<Class<? extends Annotation>>();
        annotations.add(Link.class);
        annotations.add(Edge.class);
        annotations.add(Embedded.class);
        annotations.add(FromVertex.class);
        annotations.add(ToVertex.class);

        ASSOCIATION_ANNOTATIONS = Collections.unmodifiableSet(annotations);

        annotations = new HashSet<>();
        annotations.add(OrientdbId.class);

        ID_ANNOTATIONS = Collections.unmodifiableSet(annotations);
    }

    private final Boolean isIdProperty;
    private final Boolean isAssociation;

    /**
     * Creates a new {@link OrientdbPersistentProperty}
     *
     * @param property         must not be {@literal null}.
     * @param owner            must not be {@literal null}.
     * @param simpleTypeHolder must not be {@literal null}.
     */
    public OrientdbPersistentProperty(final Property property,
                                      final PersistentEntity<?, OrientdbPersistentProperty> owner,
                                      SimpleTypeHolder simpleTypeHolder) {

        super(property, owner, simpleTypeHolder);

        this.isAssociation = ASSOCIATION_ANNOTATIONS.stream().anyMatch(this::isAnnotationPresent);
        this.isIdProperty = ID_ANNOTATIONS.stream().anyMatch(it -> isAnnotationPresent(it));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.AnnotationBasedPersistentProperty#isIdProperty()
     */
    @Override
    public boolean isIdProperty() {
        return isIdProperty;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.AnnotationBasedPersistentProperty#isAssociation()
     */
    @Override
    public boolean isAssociation() {
        return isAssociation;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.AbstractPersistentProperty#createAssociation()
     */
    @Override
    protected Association<OrientdbPersistentProperty> createAssociation() {
        return new Association<>(this, null);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.AnnotationBasedPersistentProperty#isVersionProperty()
     */
    @Override
    public boolean isVersionProperty() {
        return isAnnotationPresent(Version.class);
    }

}
