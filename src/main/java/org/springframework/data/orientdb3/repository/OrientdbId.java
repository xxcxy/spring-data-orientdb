package org.springframework.data.orientdb3.repository;

import org.springframework.data.orientdb3.repository.support.OrientdbIdParser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare a entity property is Identifier.
 *
 * @author xxcxy
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OrientdbId {

    /**
     * Configures a {@link OrientdbIdParser} to parse the id.
     *
     * @return
     */
    Class<?>[] parseBy() default {};
}
