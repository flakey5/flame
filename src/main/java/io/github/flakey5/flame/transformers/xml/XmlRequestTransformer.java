package io.github.flakey5.flame.transformers.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.flakey5.flame.transformers.IRequestTransformer;
import spark.Request;

public class XmlRequestTransformer implements IRequestTransformer {
    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public Object handle(Request request, Class<?> clazz) {
        try {
            return this.xmlMapper.readValue(request.body(), clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
