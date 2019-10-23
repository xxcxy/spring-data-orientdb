package org.springframework.data.orientdb3.support;


import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.record.OElement;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;
import org.springframework.data.orientdb3.repository.support.PropertyHandler;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EntityProxy<T> implements MethodInterceptor {

    private final T target;
    private final OElement oElement;
    private final OrientdbEntityInformation info;
    private final Map<String, Object> updateField;

    public EntityProxy(final T target, final OElement oElement, final OrientdbEntityInformation info) {
        this.target = target;
        this.oElement = oElement;
        this.info = info;
        this.updateField = new HashMap<>();
    }

    public T getProxyInstance() {
        Enhancer en = new Enhancer();
        en.setSuperclass(target.getClass());
        en.setCallback(this);
        en.setInterfaces(new Class[]{EntityProxyInterface.class});
        return (T) en.create();

    }

    @Override
    public Object intercept(final Object o, final Method method, final Object[] objects,
                            final MethodProxy methodProxy) throws Throwable {
        String methodName = method.getName();
        if (methodName.equals("saveOElement") && objects[0] instanceof ODatabaseSession) {
            return saveOElement((ODatabaseSession) objects[0], (String) objects[1]);
        } else if (methodName.equals("deleteOElement")) {
            deleteOElement();
        } else if (methodName.startsWith("get")) {
            String fieldName = getFieldName(methodName);
            if (info.hasFieldName(fieldName)) {
                if (updateField.containsKey(fieldName)) {
                    return updateField.get(fieldName);
                } else {
                    PropertyHandler ph = info.getPropertyHandler(fieldName);
                    return ph.convertToJavaProperty(oElement);
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

    private OElement saveOElement(final ODatabaseSession session, final String cluster) {
        for (Map.Entry<String, Object> field : updateField.entrySet()) {
            info.getPropertyHandler(field.getKey())
                    .setOElementProperty(oElement, field.getValue(), session);
        }
        if (cluster != null) {
            oElement.save(cluster);
        } else {
            oElement.save();
        }
        return oElement;
    }

    private void deleteOElement() {
        oElement.delete();
    }

    private String getFieldName(final String methodName) {
        return StringUtils.uncapitalize(methodName.substring(3));
    }
}
