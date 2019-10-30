package org.springframework.data.orientdb3.repository;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare finder queries directly on repository methods.
 *
 * @author xxcxy
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /**
     * Defines the query to be executed when the annotated method is called.
     *
     * @return
     */
    String value() default "";

    /**
     * Defines a special count query that shall be used for pagination queries to lookup the total number of elements for
     * a page. If non is configured we will derive the count query from the method name.
     */
    String countQuery() default "";
}
