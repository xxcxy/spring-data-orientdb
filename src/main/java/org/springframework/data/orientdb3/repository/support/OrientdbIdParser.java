package org.springframework.data.orientdb3.repository.support;

import com.orientechnologies.orient.core.id.ORID;

/**
 * A parser of conversion between java id and {@link ORID}.
 *
 * @author xxcxy
 */
public interface OrientdbIdParser<T> {
    /**
     * Converts a java id to a {@link ORID}.
     *
     * @param t
     * @return
     */
    ORID parseJavaId(T t);

    /**
     * Converts a {@link ORID} to a java id.
     *
     * @param orid
     * @return
     */
    T parseOrientdbId(ORID orid);

    /**
     * Gets Id class.
     *
     * @return
     */
    Class<T> getIdClass();
}
