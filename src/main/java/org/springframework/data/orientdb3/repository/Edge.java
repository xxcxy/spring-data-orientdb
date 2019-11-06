package org.springframework.data.orientdb3.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare a vertex property is a edgeEntity.
 *
 * @author xxcxy
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Edge {
    String INCOMING = "INCOMING";
    String OUTGOING = "OUTGOING";

    /**
     * Configures the edge.
     *
     * @return
     */
    String name() default "";

    /**
     * Configures the direction of the edge.
     *
     * @return
     */
    String direction() default OUTGOING;

    /**
     * Configures the cascade of the edge.
     *
     * @return
     */
    boolean cascade() default false;
}
