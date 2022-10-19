package io.github.flakey5.flame.annotations;

import io.github.flakey5.flame.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an endpoint.
 * <p>Valid parameter combinations the method can accept:
 *  - ({@link spark.Request})
 *  - ({@link spark.Request}, {@link spark.Response})
 *  - ({@link spark.Request}, {@link spark.Response}, {@link Class})
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Endpoint {
    /**
     * @return Endpoint path. Ex/ `/123`
     */
    String path();

    /**
     * @return {@link HttpMethod} to map this endpoint to
     */
    HttpMethod method() default HttpMethod.GET;

    /**
     * @return Content type of the data returned in the response body
     */
    String contentType() default "application/json";
}
