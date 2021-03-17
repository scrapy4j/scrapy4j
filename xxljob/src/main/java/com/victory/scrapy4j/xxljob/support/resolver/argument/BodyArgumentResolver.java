package com.victory.scrapy4j.xxljob.support.resolver.argument;


import com.victory.scrapy4j.xxljob.support.Registry;

import java.util.Map;

public class BodyArgumentResolver extends MapArgumentResolver {
    public BodyArgumentResolver(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public Object resolve(Registry registry, Object object) {
        Object body = null;
        if (object instanceof String) {
            body = object;
        } else if (object instanceof Map) {
            body = (Map) super.resolve(registry, object);
        }
        return body;
    }
}
