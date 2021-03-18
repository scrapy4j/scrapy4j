package org.scrapy4j.core.support.mybatis.core.injector;

import org.scrapy4j.core.support.mybatis.method.AbstractMethod;
import org.scrapy4j.core.support.mybatis.toolkit.ArrayUtils;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

public abstract class AbstractSqlInjector implements ISqlInjector {
    private static final Log logger = LogFactory.getLog(AbstractSqlInjector.class);

    public AbstractSqlInjector() {
    }

    public abstract List<AbstractMethod> getMethodList(Class<?> mapperClass);

    protected Class<?> extractModelClass(Class<?> mapperClass) {
        Type[] types = mapperClass.getGenericInterfaces();
        ParameterizedType target = null;
        Type[] var4 = types;
        int var5 = types.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Type type = var4[var6];
            if (type instanceof ParameterizedType) {
                Type[] typeArray = ((ParameterizedType)type).getActualTypeArguments();
                if (ArrayUtils.isNotEmpty(typeArray)) {
                    int var10 = typeArray.length;
                    byte var11 = 0;
                    if (var11 < var10) {
                        Type t = typeArray[var11];
                        if (!(t instanceof TypeVariable) && !(t instanceof WildcardType)) {
                            target = (ParameterizedType)type;
                        }
                    }
                }
                break;
            }
        }

        return target == null ? null : (Class)target.getActualTypeArguments()[0];
    }
}
