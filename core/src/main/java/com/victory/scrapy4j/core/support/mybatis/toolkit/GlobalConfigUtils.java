package com.victory.scrapy4j.core.support.mybatis.toolkit;

import cn.hutool.core.lang.Assert;
import com.victory.scrapy4j.core.support.mybatis.core.MybatisConfiguration;
import com.victory.scrapy4j.core.support.mybatis.core.config.GlobalConfig;
import com.victory.scrapy4j.core.support.mybatis.core.injector.ISqlInjector;
import org.apache.ibatis.session.Configuration;

import java.util.Set;

public class GlobalConfigUtils {
    public GlobalConfigUtils() {
    }

    public static GlobalConfig defaults() {
        return (new GlobalConfig()).setDbConfig(new GlobalConfig.DbConfig());
    }

    public static GlobalConfig getGlobalConfig(Configuration configuration) {
        Assert.notNull(configuration, "Error: You need Initialize MybatisConfiguration !", new Object[0]);
        return ((MybatisConfiguration)configuration).getGlobalConfig();
    }

    public static Class<?> getSuperMapperClass(Configuration configuration) {
        return getGlobalConfig(configuration).getSuperMapperClass();
    }

    public static Set<String> getMapperRegistryCache(Configuration configuration) {
        return getGlobalConfig(configuration).getMapperRegistryCache();
    }

    public static boolean isSupperMapperChildren(Configuration configuration, Class<?> mapperClass) {
        return getSuperMapperClass(configuration).isAssignableFrom(mapperClass);
    }

    public static ISqlInjector getSqlInjector(Configuration configuration) {
        return getGlobalConfig(configuration).getSqlInjector();
    }

}