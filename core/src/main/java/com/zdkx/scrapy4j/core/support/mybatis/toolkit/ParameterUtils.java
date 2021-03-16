package com.zdkx.scrapy4j.core.support.mybatis.toolkit;

import com.zdkx.scrapy4j.core.support.mybatis.core.metadata.IPage;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class ParameterUtils {
    private ParameterUtils() {
    }

    public static Optional<IPage> findPage(Object parameterObject) {
        if (parameterObject != null) {
            if (parameterObject instanceof Map) {
                Map<?, ?> parameterMap = (Map) parameterObject;
                Iterator var2 = parameterMap.entrySet().iterator();

                while (var2.hasNext()) {
                    Entry entry = (Entry) var2.next();
                    if (entry.getValue() != null && entry.getValue() instanceof IPage) {
                        return Optional.of((IPage) entry.getValue());
                    }
                }
            } else if (parameterObject instanceof IPage) {
                return Optional.of((IPage) parameterObject);
            }
        }

        return Optional.empty();
    }
}
