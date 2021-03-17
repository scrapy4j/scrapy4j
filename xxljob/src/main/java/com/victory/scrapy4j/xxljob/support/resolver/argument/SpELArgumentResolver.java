package com.victory.scrapy4j.xxljob.support.resolver.argument;


import com.victory.scrapy4j.xxljob.support.Registry;

public class SpELArgumentResolver implements ArgumentResolver<String,Object> {

    @Override
    public Object resolve(Registry registry, String obj) {
        return registry.getSharedObject(obj);
    }
}
