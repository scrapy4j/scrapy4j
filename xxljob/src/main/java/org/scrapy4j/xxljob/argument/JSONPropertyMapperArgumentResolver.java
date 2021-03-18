package org.scrapy4j.xxljob.argument;

import org.scrapy4j.xxljob.Registry;
import org.scrapy4j.xxljob.parser.JSONPropertyMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class JSONPropertyMapperArgumentResolver extends MapArgumentResolver {
    public JSONPropertyMapperArgumentResolver(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public Object resolve(Registry registry, Object object) {
        JSONPropertyMapper jsonPropertyMapper = null;
        if (object instanceof String) {
            //spilt by comma and transfer to jsonPropertyMapper in sequence
            String mapperExpression = (String) object;
            String[] split = StringUtils.split(mapperExpression, ",");
            if (split != null && split.length == 2) {
                jsonPropertyMapper = JSONPropertyMapper.builder(split[0], split[1]).build();
            }
        } else if (object instanceof Map) {
            jsonPropertyMapper = (JSONPropertyMapper) super.resolve(registry, object);
        }
        return jsonPropertyMapper;
    }
}
