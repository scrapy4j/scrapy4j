package org.scrapy4j.xxljob.argument;

import org.scrapy4j.xxljob.Registry;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractArgumentResolver<T, R, S extends AbstractArgumentResolver<T, R, S>> implements ArgumentResolver<T, R> {

    protected Map<String, ArgumentResolver> delegates = new HashMap<>();

    @Override
    public abstract R resolve(Registry registry, T obj);

    public S argumentResolver(String key, ArgumentResolver argumentResolver) {
        this.delegates.put(key, argumentResolver);
        return (S) this;
    }

    public S argumentResolvers(Map<String, ArgumentResolver> argumentResolvers) {
        this.delegates.putAll(argumentResolvers);
        return (S) this;
    }
}
