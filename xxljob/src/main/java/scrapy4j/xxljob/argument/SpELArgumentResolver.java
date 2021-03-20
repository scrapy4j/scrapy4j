package scrapy4j.xxljob.argument;


import scrapy4j.xxljob.Registry;

public class SpELArgumentResolver implements ArgumentResolver<String,Object> {

    @Override
    public Object resolve(Registry registry, String obj) {
        return registry.getSharedObject(obj);
    }
}
