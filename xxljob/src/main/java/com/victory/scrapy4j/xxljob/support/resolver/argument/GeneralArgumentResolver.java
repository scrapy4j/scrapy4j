package com.victory.scrapy4j.xxljob.support.resolver.argument;

import com.victory.scrapy4j.xxljob.support.Registry;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashMap;
import java.util.Map;

public class GeneralArgumentResolver extends AbstractArgumentResolver<Map<String, Object>, Object, GeneralArgumentResolver> {
    private Object obj;

    public GeneralArgumentResolver(Class<?> clazz) {
        try {
            this.obj = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object resolve(Registry registry, Map<String, Object> argsMap) {
        Object res = null;
        Map<String, Object> resolvedArgs = new HashMap<>();
        for (String key : argsMap.keySet()) {
            if (this.delegates.get(key) == null) {
                resolvedArgs.put(key, argsMap.get(key));
                continue;
            }
            resolvedArgs.put(key, this.delegates.get(key).resolve(registry, argsMap.get(key)));
        }
        BeanWrapper beanWrapper = new BeanWrapperImpl(res);
        for (String key : resolvedArgs.keySet()) {
            beanWrapper.setPropertyValue(key, resolvedArgs.get(key));
        }
        return res;
    }
}
