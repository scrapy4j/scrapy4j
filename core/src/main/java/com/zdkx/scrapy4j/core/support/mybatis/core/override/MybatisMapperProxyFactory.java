package com.zdkx.scrapy4j.core.support.mybatis.core.override;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ibatis.session.SqlSession;

public class MybatisMapperProxyFactory<T> {
    private final Class<T> mapperInterface;
    private final Map<Method, MybatisMapperMethod> methodCache = new ConcurrentHashMap();

    public MybatisMapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Map<Method, MybatisMapperMethod> getMethodCache() {
        return this.methodCache;
    }

    protected T newInstance(MybatisMapperProxy<T> mapperProxy) {
        return (T) Proxy.newProxyInstance(this.mapperInterface.getClassLoader(), new Class[]{this.mapperInterface}, mapperProxy);
    }

    public T newInstance(SqlSession sqlSession) {
        MybatisMapperProxy<T> mapperProxy = new MybatisMapperProxy(sqlSession, this.mapperInterface, this.methodCache);
        return this.newInstance(mapperProxy);
    }

    public Class<T> getMapperInterface() {
        return this.mapperInterface;
    }
}
