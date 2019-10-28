package org.springframework.data.orientdb3.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare EmbeddedEntity.
 *
 * @author xxcxy
 */

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmbeddedEntity {
    /**
     * Configures the entity name.
     *
     * @return
     */
    String name() default "";
}
