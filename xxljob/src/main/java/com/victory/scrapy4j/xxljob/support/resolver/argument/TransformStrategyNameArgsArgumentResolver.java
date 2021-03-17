package com.victory.scrapy4j.xxljob.support.resolver.argument;

import com.victory.scrapy4j.xxljob.support.Registry;
import com.victory.scrapy4j.xxljob.support.parser.transformstrategy.TransformStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Map;

public class TransformStrategyNameArgsArgumentResolver extends NameArgsMapArgumentResolver {
    @Override
    public Object resolve(Registry registry, Map<String, Object> map) {
        String transformStrategyName = map.get("name").toString();
        Map<String, Object> transformStrategyArgs = (Map<String, Object>) map.get("args");
        if (registry.getTransformStrategy(transformStrategyName) == null
                && registry.getSharedObject(transformStrategyName) == null) {
            throw new RuntimeException(String.format("transformStrategy could not be found:%s", transformStrategyName));
        }
        Class<? extends TransformStrategy> transformStrategyClz = registry.getTransformStrategy(transformStrategyName);
        TransformStrategy transformStrategy = null;
        if (transformStrategyClz != null) {
            transformStrategy = BeanUtils.instantiateClass(transformStrategyClz);
            if (transformStrategyArgs != null) {
                BeanWrapper beanWrapper = new BeanWrapperImpl(transformStrategy);
                for (String key : transformStrategyArgs.keySet()) {
                    beanWrapper.setPropertyValue(key, transformStrategyArgs.get(key));
                }
            }
        } else {
            transformStrategy= (TransformStrategy) super.resolve(registry,map);
        }
        return transformStrategy;
    }
}
