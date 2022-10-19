package io.github.flakey5.flame.transformers.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flakey5.flame.transformers.IRequestTransformer;
import spark.Request;

public class JsonRequestTransformer implements IRequestTransformer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object handle(Request request, Class<?> clazz) {
        try {
            return this.objectMapper.readValue(request.body(), clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
