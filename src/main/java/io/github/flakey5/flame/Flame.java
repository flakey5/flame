package io.github.flakey5.flame;

import io.github.flakey5.flame.annotations.Controller;
import io.github.flakey5.flame.annotations.Endpoint;
import io.github.flakey5.flame.models.FlameException;
import io.github.flakey5.flame.transformers.IRequestTransformer;
import io.github.flakey5.flame.transformers.json.JsonRequestTransformer;
import io.github.flakey5.flame.transformers.json.JsonResponseTransformer;
import io.github.flakey5.flame.transformers.xml.XmlRequestTransformer;
import io.github.flakey5.flame.transformers.xml.XmlResponseTransformer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spark.ResponseTransformer;
import spark.Route;
import spark.Spark;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Flame {
    @Getter
    private static final Map<String, IRequestTransformer> requestTransformers = new HashMap<>();
    @Getter
    private static final Map<String, ResponseTransformer> responseTransformers = new HashMap<>();
    @Getter
    private static final List<Class<?>> mappedControllers = new ArrayList<>();

    static {
        // Register all default request & response transformers
        Flame.requestTransformers.put("application/json", new JsonRequestTransformer());
        Flame.responseTransformers.put("application/json", new JsonResponseTransformer());

        XmlRequestTransformer xmlRequestTransformer = new XmlRequestTransformer();
        XmlResponseTransformer xmlResponseTransformer = new XmlResponseTransformer();
        Flame.requestTransformers.put("application/xml", xmlRequestTransformer);
        Flame.responseTransformers.put("application/xml", xmlResponseTransformer);
        Flame.requestTransformers.put("text/xml", xmlRequestTransformer);
        Flame.responseTransformers.put("text/xml", xmlResponseTransformer);
    }

    /**
     * Finds all classes in a given package and runs them through {@link #mapController(Class)}
     * @param packageName Package to look through
     */
    @SuppressWarnings("unused")
    public static void mapControllerPackage(String packageName) {
        ReflectionUtil.getClassesInPackage(packageName).forEach(Flame::mapController);
    }

    /**
     * Finds all methods annotated with {@link Endpoint} in a class annotated
     * with {@link Controller} and maps them with the correct {@link Spark} method.
     * Also takes care of boiler plate things such as parsing the body into Java class instances.
     *
     * @param clazz Class to look through
     */
    public static void mapController(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Controller.class))
            return;

        Controller controller = clazz.getDeclaredAnnotation(Controller.class);
        if (controller.disabled())
            return;

        Spark.path(controller.prefix(), () -> {
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(Endpoint.class))
                    continue;

                Endpoint endpoint = method.getAnnotation(Endpoint.class);

                // Validate we have a transformer for handling the response body unless we're dealing with a primitive or string
                if (!Flame.responseTransformers.containsKey(endpoint.contentType())
                        && Flame.needsTransformer(method.getReturnType()))
                    throw new RuntimeException("No transformer exists for content type " + endpoint.contentType() + " for endpoint method " + method.getName());

                Parameter[] parameters = method.getParameters();
                if (parameters.length == 0)
                    throw new RuntimeException("Endpoint method " + method.getName() + " needs to take in Request parameter");

                Route route = (req, res) -> {
                    res.type(endpoint.contentType());
                    if (method.getReturnType() == void.class)
                        res.status(204);

                    try {
                        List<Object> params = new ArrayList<>();
                        params.add(req);
                        if (parameters.length >= 2)
                            params.add(res);

                        if ((endpoint.method().equals(HttpMethod.POST)
                                || endpoint.method().equals(HttpMethod.PATCH)
                                || endpoint.method().equals(HttpMethod.PUT))
                                && parameters.length >= 3
                                && req.body().length() != 0) {
                            if (req.contentType() == null)
                                Spark.halt(406);

                            Class<?> bodyType = parameters[2].getType();
                            if (Flame.requestTransformers.containsKey(req.contentType())) {
                                Object body = Flame.requestTransformers.get(req.contentType()).handle(req, bodyType);
                                params.add(body);
                            } else {
                                Spark.halt(406);
                            }
                        }

                        Object response = method.invoke(
                                Modifier.isStatic(method.getModifiers()) ? null : clazz.getDeclaredConstructor().newInstance(),
                                params.toArray()
                        );

                        return Objects.requireNonNullElse(response, "");
                    } catch (FlameException exception) {
                        return exception;
                    }
                };

                boolean needsTransformer = Flame.needsTransformer(method.getReturnType());
                switch (endpoint.method()) {
                    case GET -> {
                        if (needsTransformer)
                            Spark.get(endpoint.path(), endpoint.contentType(), route, Flame.responseTransformers.get(endpoint.contentType()));
                        else
                            Spark.get(endpoint.path(), route);
                    }
                    case POST -> {
                        if (needsTransformer)
                            Spark.post(endpoint.path(), endpoint.contentType(), route, Flame.responseTransformers.get(endpoint.contentType()));
                        else
                            Spark.post(endpoint.path(), route);
                    }
                    case PATCH -> {
                        if (needsTransformer)
                            Spark.patch(endpoint.path(), endpoint.contentType(), route, Flame.responseTransformers.get(endpoint.contentType()));
                        else
                            Spark.patch(endpoint.path(), route);
                    }
                    case PUT -> {
                        if (needsTransformer)
                            Spark.put(endpoint.path(), endpoint.contentType(), route, Flame.responseTransformers.get(endpoint.contentType()));
                        else
                            Spark.put(endpoint.path(), route);
                    }
                    case DELETE -> {
                        if (needsTransformer)
                            Spark.delete(endpoint.path(), endpoint.contentType(), route, Flame.responseTransformers.get(endpoint.contentType()));
                        else
                            Spark.delete(endpoint.path(), route);
                    }
                }
            }
        });

        Flame.mappedControllers.add(clazz);
    }

    private static boolean needsTransformer(Class<?> clazz) {
        return !clazz.isPrimitive() && clazz != String.class;
    }
}
