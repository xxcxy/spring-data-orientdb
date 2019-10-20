package org.springframework.data.orientdb3.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityProperty {

    String name() default "";

    String min() default "";

    String max() default "";

    boolean mandatory() default false;

    boolean readonly() default false;

    boolean notNull() default false;

    String regexp() default "";
}
