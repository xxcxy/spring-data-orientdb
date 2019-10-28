package org.springframework.data.orientdb3.support;


import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;
import org.springframework.data.orientdb3.repository.support.PropertyHandler;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.orientdb3.support.SessionListener.bindEntityProxy;
import static org.springframework.util.ReflectionUtils.setField;

/**
 * A entity proxy that intercepting access to the target object and get or set a {@link OElement} property.
 *
 * @param <T>
 * @author xxcxy
 */
public class EntityProxy<T> implements MethodInterceptor {

    private final T target;
    private final OElement oElement;
    private final OrientdbEntityInformation<T, ?> info;
    private final Map<String, Object> updateField;
    private final Map<OElement, Object> gotObjects;
    private boolean inSaving;
    private boolean isLoaded;

    /**
     * Creates a new {@link EntityProxy}.
     *
     * @param target
     * @param oElement
     * @param info
     * @param gotObjects
     */
    public EntityProxy(final T target, final OElement oElement, final OrientdbEntityInformation info,
                       final Map<OElement, Object> gotObjects) {
        this.target = target;
        this.oElement = oElement;
        this.info = info;
        this.updateField = new HashMap<>();
        this.gotObjects = gotObjects;
        this.inSaving = false;
        this.isLoaded = false;
    }

    /**
     * Gets a proxyInstance.
     *
     * @return
     */
    public T getProxyInstance() {
        Enhancer en = new Enhancer();
        en.setSuperclass(target.getClass());
        en.setCallback(this);
        en.setInterfaces(new Class[]{EntityProxyInterface.class});
        T t = (T) en.create();
        bindEntityProxy((EntityProxyInterface) t);
        return t;
    }

    /*
     * (non-Javadoc)
     * @see  org.springframework.cglib.proxy#intercept
     */
    @Override
    public Object intercept(final Object o, final Method method, final Object[] objects,
                            final MethodProxy methodProxy) throws Throwable {
        String methodName = method.getName();
        if (methodName.equals("saveOElement") && objects[0] instanceof ODatabaseSession) {
            return saveOElement((ODatabaseSession) objects[0], (String) objects[1]);
        } else if (methodName.equals("deleteOElement")) {
            deleteOElement();
        } else if (methodName.equals("findOElement")) {
            return oElement;
        } else if (methodName.equals("loadId")) {
            loadId();
        } else if (methodName.equals("loadStable")) {
            if (!isLoaded) {
                load(o);
            }
        } else if (isLoaded) {
            return method.invoke(target, objects);
        } else if (methodName.startsWith("get")) {
            String fieldName = getFieldName(methodName);
            if (info.isId(fieldName)) {
                return info.getId(oElement);
            } else if (info.hasFieldName(fieldName)) {
                if (updateField.containsKey(fieldName)) {
                    return updateField.get(fieldName);
                } else {
                    return getPropertyValue(o, info.getPropertyHandler(fieldName), fieldName);
                }
            }
        } else if (methodName.startsWith("set")) {
            String fieldName = getFieldName(methodName);
            if (info.hasFieldName(fieldName)) {
                updateField.put(fieldName, objects[0]);
            }
        } else {
            return method.invoke(target, objects);
        }
        return null;
    }

    /**
     * Loads value to the target field.
     */
    private void load(final Object entityProxy) {
        isLoaded = true;
        for (PropertyHandler ph : info.getAllPropertyHandlers()) {
            Field field = ph.getPropertyField();

            if (updateField.containsKey(field.getName())) {
                setField(field, target, updateField.get(field.getName()));
            } else {
                Object value = getPropertyValue(entityProxy, ph, field.getName());
                setField(field, target, value);
                setPropertyLoad(value);
            }
        }
    }

    /**
     * Loads target id field.
     */
    private void loadId() {
        info.setId(target, oElement);
    }

    /**
     * Loads field object.
     *
     * @param property
     */
    private void setPropertyLoad(final Object property) {
        if (property instanceof EntityProxyInterface) {
            ((EntityProxyInterface) property).loadStable();
        } else if (property instanceof Collection) {
            ((Collection) property).forEach(this::setPropertyLoad);
        } else if (property instanceof Map) {
            ((Map) property).values().forEach(this::setPropertyLoad);
        }
    }

    /**
     * Gets property value.
     *
     * @param entityProxy
     * @param ph
     * @param fieldName
     * @return
     */
    private Object getPropertyValue(final Object entityProxy, final PropertyHandler ph, final String fieldName) {
        if (updateField.containsKey(fieldName)) {
            return updateField.get(fieldName);
        } else {
            if (!gotObjects.containsKey(oElement)) {
                gotObjects.put(oElement, entityProxy);
            }
            Object obj = ph.getPropertyInJavaType(oElement, gotObjects);
            // If a collection be got, we can not listen its changed but we can assume that it will be changed.
            if (obj instanceof Collection || obj instanceof Map) {
                updateField.put(fieldName, obj);
            }
            return obj;
        }
    }

    /**
     * Saves the {@link OElement}.
     *
     * @param session
     * @param cluster
     * @return
     */
    private OElement saveOElement(final ODatabaseSession session, final String cluster) {
        if (inSaving) {
            return oElement;
        }
        if (isLoaded) {
            return ((EntityProxyInterface) info.save(target, session, cluster, new HashMap<>())).findOElement();
        }
        inSaving = true;
        Set<Object> updatedTopProperty = updateField.keySet()
                .stream()
                .map(info::getPropertyHandler)
                .map(p -> p.getPropertyInJavaType(oElement, gotObjects))
                .collect(Collectors.toSet());

        for (Map.Entry<String, Object> field : updateField.entrySet()) {
            info.getPropertyHandler(field.getKey())
                    .setOElementProperty(oElement, field.getValue(), session, new HashMap<>());
        }
        for (Object obj : gotObjects.values()) {
            if (obj != this && obj instanceof EntityProxyInterface && !updatedTopProperty.contains(obj)) {
                ((EntityProxyInterface) obj).saveOElement(session, null);
            }
        }
        if (cluster != null) {
            oElement.save(cluster);
        } else {
            oElement.save();
        }
        updateField.clear();
        inSaving = false;
        return oElement;
    }

    /**
     * Deletes the {@link OElement}.
     */
    private void deleteOElement() {
        oElement.delete();
    }

    /**
     * Gets the field name for a given method name.
     *
     * @param methodName
     * @return
     */
    private String getFieldName(final String methodName) {
        return StringUtils.uncapitalize(methodName.substring(3));
    }
}
