package org.springframework.data.orientdb3.support;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.orientdb3.repository.Edge;
import org.springframework.data.orientdb3.repository.EdgeEntity;
import org.springframework.data.orientdb3.repository.ElementEntity;
import org.springframework.data.orientdb3.repository.EmbeddedEntity;
import org.springframework.data.orientdb3.repository.EntityProperty;
import org.springframework.data.orientdb3.repository.FromVertex;
import org.springframework.data.orientdb3.repository.Link;
import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.repository.ToVertex;
import org.springframework.data.orientdb3.repository.VertexEntity;
import org.springframework.data.orientdb3.repository.support.EntityType;
import org.springframework.data.orientdb3.repository.util.Constants;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE.UNIQUE;
import static org.springframework.util.StringUtils.capitalize;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * A session Factory.
 *
 * @author xxcxy
 */
public class SessionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFactory.class);

    private final OrientDB orientDB;
    private final ODatabasePool pool;
    private final IOrientdbConfig orientdbConfig;

    /**
     * Creates a new {@link SessionFactory}.
     *
     * @param orientdbConfig
     */
    public SessionFactory(final IOrientdbConfig orientdbConfig) {
        if (orientdbConfig.getAutoGenerateSchema()) {
            generateSchema(orientdbConfig);
        }
        orientDB = new OrientDB(orientdbConfig.getHosts(), orientdbConfig.getDatabaseUsername(),
                orientdbConfig.getDatabasePassword(), OrientDBConfig.defaultConfig());
        pool = new ODatabasePool(orientDB, orientdbConfig.getDatabaseName(), orientdbConfig.getUsername(),
                orientdbConfig.getPassword());
        this.orientdbConfig = orientdbConfig;
    }

    /**
     * Open a {@link ODatabaseSession}.
     *
     * @return
     */
    public ODatabaseSession openSession() {
        ODatabaseSession session = pool.acquire();
        session.registerListener(new SessionListener());
        return session;
    }

    /**
     * Generates the orientdb schema.
     *
     * @param orientdbConfig
     */
    public void generateSchema(final IOrientdbConfig orientdbConfig) {
        OrientDB db = new OrientDB(orientdbConfig.getHosts(), orientdbConfig.getDatabaseUsername(),
                orientdbConfig.getDatabasePassword(), OrientDBConfig.defaultConfig());
        ODatabaseSession session = db.open(orientdbConfig.getDatabaseName(), orientdbConfig.getUsername(),
                orientdbConfig.getPassword());
        Map<String, OClass> processed = new HashMap<>();
        Map<String, Consumer<OClass>> postProcess = new HashMap<>();
        for (Class clazz : getClasses(orientdbConfig.getEntityScanPackage())) {
            generateSchema(session, clazz, processed, postProcess);
        }

        // Set relationships if a class was processed before it's relation class.
        for (String className : postProcess.keySet()) {
            postProcess.get(className).accept(processed.get(className));
        }
        session.close();
        db.close();
    }

    /**
     * Scans a package and find all designated classes.
     *
     * @param scanPackage
     * @return
     */
    private List<Class> getClasses(final String scanPackage) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(ElementEntity.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(VertexEntity.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(EdgeEntity.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(EmbeddedEntity.class));
        Set<BeanDefinition> beanDefinitionSet = provider.findCandidateComponents(scanPackage);
        List<Class> entityClasses = new ArrayList<>();
        for (BeanDefinition beanDefinition : beanDefinitionSet) {
            String beanClassName = beanDefinition.getBeanClassName();
            try {
                entityClasses.add(Class.forName(beanClassName));
            } catch (ClassNotFoundException e) {
                LOGGER.error("Generate class: {}'s schema error: ", beanClassName, e);
            }
        }
        return entityClasses;
    }

    /**
     * Generates the orientdb schema.
     *
     * @param session
     * @param clazz
     * @param processed
     * @param postProcess
     */
    private void generateSchema(final ODatabaseSession session, final Class<?> clazz,
                                final Map<String, OClass> processed, final Map<String, Consumer<OClass>> postProcess) {
        String className = getClassName(clazz);
        String superClassName = null;
        if (processed.containsKey(className)) {
            return;
        }
        if (!clazz.getSuperclass().equals(Object.class)) {
            generateSchema(session, clazz.getSuperclass(), processed, postProcess);
            superClassName = getClassName(clazz.getSuperclass());
        }

        // If schema is generated add the OClass and return
        OClass existed = session.getClass(className);
        if (existed != null) {
            processed.put(className, existed);
            return;
        }

        OClass oClass = getOClass(session, clazz, className, superClassName);

        // Set property
        ReflectionUtils.doWithFields(clazz, field -> {
            // If the field inherits from super class, should not set it again.
            if (!field.getDeclaringClass().equals(clazz)) {
                return;
            }
            field.setAccessible(true);
            OProperty oProperty = null;
            OType oType = OType.getTypeByClass(field.getType());
            if (oType == null) {
                oType = Constants.TYPES_BY_CLASS.getOrDefault(field.getType(), OType.EMBEDDED);
            }

            EntityProperty entityProperty = field.getAnnotation(EntityProperty.class);
            String propertyName = getPropertyName(entityProperty, field.getName());

            if (field.getAnnotation(Edge.class) != null) {

                // edge is not a OClass property but a Edge OClass
                handleEdgeProperty(session, field.getAnnotation(Edge.class), field, processed);

            } else if (field.getAnnotation(FromVertex.class) != null
                    || field.getAnnotation(ToVertex.class) != null
                    || field.getAnnotation(OrientdbId.class) != null) {
                // fromVertex, toVertex, ID are not the Entity's property
            } else if (Constants.OBJECT_TYPE.containsKey(oType)) {
                OType actualType = getActualType(oType, field);
                OClass relateClass = processed.get(getClassName(field.getType()));
                if (relateClass != null) {
                    oProperty = oClass.createProperty(propertyName, actualType, relateClass);
                } else {
                    // If the relate class has not create put the processing to postProcess map
                    Consumer<OClass> postCreateProperty = oc -> {
                        OProperty op = oClass.createProperty(propertyName, actualType, oc);
                        setPropertyConstraints(op, entityProperty);
                    };
                    postProcess.put(getClassName(field.getType()), postCreateProperty);
                }
            } else {
                oProperty = oClass.createProperty(propertyName, oType);
            }
            if (oProperty != null) {
                setPropertyConstraints(oProperty, entityProperty);
            }

        });
        processed.put(className, oClass);
    }

    /**
     * Gets a {@link OType}.
     *
     * @param oType
     * @param field
     * @return
     */
    private OType getActualType(final OType oType, final Field field) {
        if (field.getAnnotation(Link.class) != null) {
            return Constants.OBJECT_TYPE.get(oType);
        }
        return oType;
    }

    /**
     * Handles a edge property.
     *
     * @param session
     * @param edge
     * @param field
     * @param processed
     */
    private void handleEdgeProperty(final ODatabaseSession session, final Edge edge,
                                    final Field field, final Map<String, OClass> processed) {
        String edgeName = edge.name();
        if (isEmpty(edgeName)) {
            edgeName = capitalize(field.getName());
        }
        if (processed.containsKey(edgeName)) {
            return;
        }
        OClass oClass = session.getClass(edgeName);
        if (oClass != null) {
            processed.put(edgeName, oClass);
        } else {
            processed.put(edgeName, session.createEdgeClass(edgeName));
        }
    }

    /**
     * Sets the property constraints.
     *
     * @param op
     * @param entityProperty
     */
    private void setPropertyConstraints(final OProperty op, final @Nullable EntityProperty entityProperty) {
        if (entityProperty != null) {
            if (!isEmpty(entityProperty.min())) {
                op.setMin(entityProperty.min());
            }
            if (!isEmpty(entityProperty.max())) {
                op.setMax(entityProperty.max());
            }
            if (!isEmpty(entityProperty.regexp())) {
                op.setRegexp(entityProperty.regexp());
            }
            if (entityProperty.unique()) {
                op.createIndex(UNIQUE);
            }
            op.setNotNull(entityProperty.notNull());
            op.setMandatory(entityProperty.mandatory());
            op.setReadonly(entityProperty.readonly());
        }
    }

    /**
     * Gets the property name.
     *
     * @param entityProperty
     * @param fieldName
     * @return
     */
    private String getPropertyName(@Nullable final EntityProperty entityProperty, final String fieldName) {
        if (entityProperty != null && !isEmpty(entityProperty.name())) {
            return entityProperty.name();
        }
        return fieldName;
    }

    /**
     * Get a {@link OClass} for a given class and className.
     *
     * @param session
     * @param clazz
     * @param className
     * @param superClassName
     * @return
     */
    private OClass getOClass(final ODatabaseSession session, final Class<?> clazz,
                             final String className, @Nullable final String superClassName) {
        List<String> superClasses = new ArrayList();
        if (superClassName != null) {
            superClasses.add(superClassName);
        }
        if (clazz.getAnnotation(VertexEntity.class) != null) {
            superClasses.add("V");
        } else if (clazz.getAnnotation(EdgeEntity.class) != null) {
            superClasses.add("E");
        }
        if (superClasses.size() > 0) {
            return session.createClass(className, superClasses.toArray(new String[superClasses.size()]));
        } else {
            return session.createClass(className);
        }
    }

    /**
     * Gets a class name for a given class.
     *
     * @param clazz
     * @return
     */
    private String getClassName(final Class<?> clazz) {
        return EntityType.getEntityType(clazz).map(e -> e.getEntityName(clazz)).orElse(clazz.getSimpleName());
    }

    /**
     * Destroy this sessionFactory.
     */
    public void destroy() {
        pool.close();
        orientDB.close();
    }
}
