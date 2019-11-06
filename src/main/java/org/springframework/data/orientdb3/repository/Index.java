package org.springframework.data.orientdb3.repository;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare a index.
 *
 * @author xxcxy
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {
    String UNIQUE = "UNIQUE";
    String NOTUNIQUE = "NOTUNIQUE";
    String FULLTEXT = "FULLTEXT";
    String DICTIONARY = "DICTIONARY";

    /**
     * Index's name.
     *
     * @return
     */
    String name();

    /**
     * Index's columns.
     *
     * @return
     */
    String columnList();

    /**
     * Index's type.
     *
     * @return
     */
    String type() default UNIQUE;
}
