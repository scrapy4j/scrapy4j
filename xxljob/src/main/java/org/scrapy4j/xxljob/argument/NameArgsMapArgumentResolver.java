package org.scrapy4j.xxljob.argument;

import org.scrapy4j.core.utils.Utils;
import org.scrapy4j.xxljob.Registry;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Map;

public class NameArgsMapArgumentResolver extends AbstractArgumentResolver<Map<String, Object>, Object, NameArgsMapArgumentResolver> {

    @Override
    public Object resolve(Registry registry, Map<String, Object> map) {
        Object res = null;
        if (delegates != null && delegates.size() > 0) {
            return delegates.get(map.get("name")).resolve(registry, map.get("args"));
        } else {
            String name = MapUtils.getString(map, "name");
            Map<String, Object> argsMap = MapUtils.getMap(map, "args");
            if (Utils.isSPEL(name)) {
                res = registry.getSharedObject(name);
                if (argsMap != null) {//spring 单例 bean 线程安全问题，最好不要设置属性值
                    BeanWrapper beanWrapper = new BeanWrapperImpl(res);
                    for (String key : argsMap.keySet()) {
                        beanWrapper.setPropertyValue(key, argsMap.get(key));
                    }
                }
            }
        }
        return res;
    }

}