package org.springframework.data.orientdb3.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare a EdgeEntity.
 *
 * @author xxcxy
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EdgeEntity {
    /**
     * Configures the edge name.
     *
     * @return
     */
    String name() default "";

    /**
     * Configures the index.
     *
     * @return
     */
    Index[] indexes() default {};
}
