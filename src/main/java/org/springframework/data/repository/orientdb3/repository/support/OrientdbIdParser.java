package org.springframework.data.repository.orientdb3.repository.support;

import com.orientechnologies.orient.core.id.ORID;

public interface OrientdbIdParser<T> {
    ORID parseJavaId(T t);

    T parseOrientdbId(ORID orid);

    Class<T> getIdClass();
}
