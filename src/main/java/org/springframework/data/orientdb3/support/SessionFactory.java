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
import org.springframework.data.orientdb3.repository.EntityProperty;
import org.springframework.data.orientdb3.repository.FromVertex;
import org.springframework.data.orientdb3.repository.Link;
import org.springframework.data.orientdb3.repository.OrientdbId;
import org.springframework.data.orientdb3.repository.ToVertex;
import org.springframework.data.orientdb3.repository.VertexEntity;
import org.springframework.data.orientdb3.repository.exception.EntityInitException;
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

public class SessionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFactory.class);

    private final OrientDB orientDB;
    private final ODatabasePool pool;
    private final IOrientdbConfig orientdbConfig;

    public SessionFactory(final IOrientdbConfig orientdbConfig) {
        if (orientdbConfig.getAutoGenerateSchema()) {
            generateSchema(orientdbConfig);
        }
        orientDB = new OrientDB(orientdbConfig.getUrl(), orientdbConfig.getServerUser(),
                orientdbConfig.getServerPassword(), OrientDBConfig.defaultConfig());
        pool = new ODatabasePool(orientDB, orientdbConfig.getDatabase(), orientdbConfig.getUserName(),
                orientdbConfig.getPassword());
        this.orientdbConfig = orientdbConfig;
    }

    public ODatabaseSession openSession() {
        return pool.acquire();
    }

    public void generateSchema(final IOrientdbConfig orientdbConfig) {
        OrientDB db = new OrientDB(orientdbConfig.getUrl(), orientdbConfig.getServerUser(),
                orientdbConfig.getServerPassword(), OrientDBConfig.defaultConfig());
        ODatabaseSession session = db.open(orientdbConfig.getDatabase(), orientdbConfig.getUserName(),
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

    private List<Class> getClasses(final String scanPackage) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(ElementEntity.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(VertexEntity.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(EdgeEntity.class));
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

    private OType getActualType(final OType oType, final Field field) {
        if (field.getAnnotation(Link.class) != null) {
            return Constants.OBJECT_TYPE.get(oType);
        }
        return oType;
    }

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

    private String getPropertyName(@Nullable final EntityProperty entityProperty, final String fieldName) {
        if (entityProperty != null && !isEmpty(entityProperty.name())) {
            return entityProperty.name();
        }
        return fieldName;
    }

    private OClass getOClass(final ODatabaseSession session, final Class<?> clazz,
                             final String className, @Nullable final String superClassName) {
        if (clazz.getAnnotation(ElementEntity.class) != null) {
            if (superClassName != null) {
                return session.createClass(className, superClassName);
            }
            return session.createClass(className);
        } else if (clazz.getAnnotation(VertexEntity.class) != null) {
            if (session.getClass(className) == null) {
                return session.createVertexClass(className);
            }
        } else if (clazz.getAnnotation(EdgeEntity.class) != null) {
            return session.createEdgeClass(className);
        }
        throw new EntityInitException("Entity class must have one of the annotation(ElementEntity," +
                " VertexEntity, EdgeEntity");
    }

    private String getClassName(final Class<?> clazz) {
        ElementEntity elementEntity = clazz.getAnnotation(ElementEntity.class);
        if (elementEntity != null) {
            if (!isEmpty(elementEntity.name())) {
                return elementEntity.name();
            }
        }
        VertexEntity vertexEntity = clazz.getAnnotation(VertexEntity.class);
        if (vertexEntity != null) {
            if (!isEmpty(vertexEntity.name())) {
                return vertexEntity.name();
            }
        }
        EdgeEntity edgeEntity = clazz.getAnnotation(EdgeEntity.class);
        if (edgeEntity != null) {
            if (!isEmpty(edgeEntity.name())) {
                return edgeEntity.name();
            }
        }
        return clazz.getSimpleName();
    }

    public void destroy() {
        pool.close();
        orientDB.close();
    }
}
