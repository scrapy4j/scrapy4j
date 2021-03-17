//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.victory.scrapy4j.core.support.mybatis.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.victory.scrapy4j.core.support.mybatis.core.override.MybatisMapperProxyFactory;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.SqlSession;

public class MybatisMapperRegistry extends MapperRegistry {
    private final Map<Class<?>, MybatisMapperProxyFactory<?>> knownMappers = new HashMap();
    private final MybatisConfiguration config;

    public MybatisMapperRegistry(MybatisConfiguration config) {
        super(config);
        this.config = config;
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        MybatisMapperProxyFactory<T> mapperProxyFactory = (MybatisMapperProxyFactory)this.knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MybatisPlusMapperRegistry.");
        } else {
            try {
                return mapperProxyFactory.newInstance(sqlSession);
            } catch (Exception var5) {
                throw new BindingException("Error getting mapper instance. Cause: " + var5, var5);
            }
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return this.knownMappers.containsKey(type);
    }

    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (this.hasMapper(type)) {
                return;
            }

            boolean loadCompleted = false;

            try {
                this.knownMappers.put(type, new MybatisMapperProxyFactory(type));
                MybatisMapperAnnotationBuilder parser = new MybatisMapperAnnotationBuilder(this.config, type);
                parser.parse();
                loadCompleted = true;
            } finally {
                if (!loadCompleted) {
                    this.knownMappers.remove(type);
                }

            }
        }

    }

    public Collection<Class<?>> getMappers() {
        return Collections.unmodifiableCollection(this.knownMappers.keySet());
    }
}
