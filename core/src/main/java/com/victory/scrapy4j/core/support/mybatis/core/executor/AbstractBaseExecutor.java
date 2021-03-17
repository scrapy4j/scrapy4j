package com.victory.scrapy4j.core.support.mybatis.core.executor;


import com.victory.scrapy4j.core.support.mybatis.core.metadata.IPage;
import com.victory.scrapy4j.core.support.mybatis.toolkit.ParameterUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;


public abstract class AbstractBaseExecutor extends BaseExecutor {
    protected AbstractBaseExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        if (super.isClosed()) {
            throw new ExecutorException("Executor was closed.");
        } else {
            CacheKey cacheKey = new CacheKey();
            cacheKey.update(ms.getId());
            cacheKey.update(rowBounds.getOffset());
            cacheKey.update(rowBounds.getLimit());
            Optional<IPage> pageOptional = ParameterUtils.findPage(parameterObject);
            pageOptional.ifPresent((page) -> {
                cacheKey.update(page.cacheKey());
            });
            cacheKey.update(boundSql.getSql());
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
            Iterator var9 = parameterMappings.iterator();

            while(var9.hasNext()) {
                ParameterMapping parameterMapping = (ParameterMapping)var9.next();
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    String propertyName = parameterMapping.getProperty();
                    Object value;
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else {
                        MetaObject metaObject = this.configuration.newMetaObject(parameterObject);
                        value = metaObject.getValue(propertyName);
                    }

                    cacheKey.update(value);
                }
            }

            if (this.configuration.getEnvironment() != null) {
                cacheKey.update(this.configuration.getEnvironment().getId());
            }

            return cacheKey;
        }
    }
}
