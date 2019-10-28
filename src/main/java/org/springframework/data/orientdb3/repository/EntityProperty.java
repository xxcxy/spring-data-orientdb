package org.springframework.data.orientdb3.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare entityProperty.
 *
 * @author xxcxy
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityProperty {

    /**
     * Configures the property name.
     *
     * @return
     */
    String name() default "";

    /**
     * Configures the property min constraint.
     *
     * @return
     */
    String min() default "";

    /**
     * Configures the property max constraint.
     *
     * @return
     */
    String max() default "";

    /**
     * Configures the property mandatory constraint.
     *
     * @return
     */
    boolean mandatory() default false;

    /**
     * Configures the property readonly constraint.
     *
     * @return
     */
    boolean readonly() default false;

    /**
     * Configures the property notNull constraint.
     *
     * @return
     */
    boolean notNull() default false;

    /**
     * Configures the property unique constraint.
     *
     * @return
     */
    boolean unique() default false;

    /**
     * Configure the property regexp constraint.
     *
     * @return
     */
    String regexp() default "";
}
