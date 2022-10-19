package io.github.flakey5.flame.transformers.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import spark.ResponseTransformer;

public class XmlResponseTransformer implements ResponseTransformer {
    private final XmlMapper objectMapper = new XmlMapper();

    @Override
    public String render(Object model) throws Exception {
        return this.objectMapper.writeValueAsString(model);
    }
}
