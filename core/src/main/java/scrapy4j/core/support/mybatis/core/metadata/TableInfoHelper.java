package scrapy4j.core.support.mybatis.core.metadata;

import scrapy4j.core.support.mybatis.toolkit.ClassUtils;
import scrapy4j.core.support.mybatis.toolkit.ReflectionKit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TableInfoHelper {
    private static final Map<Class<?>, TableInfo> TABLE_INFO_CACHE = new ConcurrentHashMap();

    public TableInfoHelper() {
    }

    public static TableInfo getTableInfo(Class<?> clazz) {
        if (clazz != null && !ReflectionKit.isPrimitiveOrWrapper(clazz) && clazz != String.class) {
            TableInfo tableInfo = TABLE_INFO_CACHE.get(ClassUtils.getUserClass(clazz));
            if (null != tableInfo) {
                return tableInfo;
            } else {
                for(Class currentClass = clazz; null == tableInfo && Object.class != currentClass; tableInfo = TABLE_INFO_CACHE.get(ClassUtils.getUserClass(currentClass))) {
                    currentClass = currentClass.getSuperclass();
                }

                if (tableInfo != null) {
                    TABLE_INFO_CACHE.put(ClassUtils.getUserClass(clazz), tableInfo);
                }

                return tableInfo;
            }
        } else {
            return null;
        }
    }
}
