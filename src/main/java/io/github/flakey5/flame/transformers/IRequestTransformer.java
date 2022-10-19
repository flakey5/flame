package io.github.flakey5.flame.transformers;

import spark.Request;

@FunctionalInterface
public interface IRequestTransformer {
    Object handle(Request request, Class<?> bodyType);
}
