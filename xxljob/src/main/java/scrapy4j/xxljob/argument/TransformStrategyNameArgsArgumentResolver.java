package scrapy4j.xxljob.argument;

import org.apache.commons.collections.MapUtils;
import scrapy4j.core.utils.Utils;
import scrapy4j.xxljob.Registry;
import scrapy4j.xxljob.parser.transformstrategy.TransformStrategy;
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
                    String val = MapUtils.getString(transformStrategyArgs, key);
                    if (Utils.isSPEL(val)) {
                        beanWrapper.setPropertyValue(key, registry.getSharedObject(val));
                    } else {
                        beanWrapper.setPropertyValue(key, transformStrategyArgs.get(key));
                    }
                }
            }
        } else {
            transformStrategy = (TransformStrategy) super.resolve(registry, map);
        }
        return transformStrategy;
    }
}
