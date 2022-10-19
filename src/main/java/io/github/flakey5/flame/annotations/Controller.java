package io.github.flakey5.flame.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a controller.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    /**
     * @return Route prefix, ex/ "/v1"
     */
    String prefix() default "";

    /**
     * @return Whether the controller and all its endpoints are disabled and shouldn't be mapped.
     */
    boolean disabled() default false;
}
