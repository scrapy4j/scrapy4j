package com.victory.scrapy4j.core.support.mybatis.core.executor;

import com.victory.scrapy4j.core.support.mybatis.core.metadata.IPage;
import com.victory.scrapy4j.core.support.mybatis.core.metadata.PageList;
import com.victory.scrapy4j.core.support.mybatis.toolkit.ParameterUtils;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class MybatisCachingExecutor implements Executor {
    private final Executor delegate;
    private final TransactionalCacheManager tcm = new TransactionalCacheManager();

    public MybatisCachingExecutor(Executor delegate) {
        this.delegate = delegate;
        delegate.setExecutorWrapper(this);
    }

    public Transaction getTransaction() {
        return this.delegate.getTransaction();
    }

    public void close(boolean forceRollback) {
        try {
            if (forceRollback) {
                this.tcm.rollback();
            } else {
                this.tcm.commit();
            }
        } finally {
            this.delegate.close(forceRollback);
        }

    }

    public boolean isClosed() {
        return this.delegate.isClosed();
    }

    public int update(MappedStatement ms, Object parameterObject) throws SQLException {
        this.flushCacheIfRequired(ms);
        return this.delegate.update(ms, parameterObject);
    }

    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        CacheKey key = this.createCacheKey(ms, parameterObject, rowBounds, boundSql);
        return this.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }

    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        this.flushCacheIfRequired(ms);
        return this.delegate.queryCursor(ms, parameter, rowBounds);
    }

    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        Cache cache = ms.getCache();
        Optional<IPage> pageOptional = ParameterUtils.findPage(parameterObject);
        if (cache != null) {
            this.flushCacheIfRequired(ms);
            if (ms.isUseCache() && resultHandler == null) {
                this.ensureNoOutParams(ms, boundSql);
                Object result = this.tcm.getObject(cache, key);
                IPage page;
                CacheKey cacheKey;
                Number count;
                if (result == null) {
                    if (pageOptional.isPresent()) {
                        page = (IPage) pageOptional.get();
                        cacheKey = null;
                        if (page.isSearchCount()) {
                            cacheKey = this.getCountCacheKey(ms, boundSql, parameterObject, RowBounds.DEFAULT);
                            count = (Number) this.tcm.getObject(cache, cacheKey);
                            if (count != null) {
                                page.hitCount(true);
                                page.setTotal(count.longValue());
                            }
                        }

                        result = this.delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                        List<E> records = (List) result;
                        page.setRecords(records);
                        this.tcm.putObject(cache, key, records);
                        if (cacheKey != null && !page.isHitCount()) {
                            this.tcm.putObject(cache, cacheKey, page.getTotal());
                        }

                        return new PageList(records, page.getTotal());
                    }

                    result = this.delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                    this.tcm.putObject(cache, key, result);
                    return (List) result;
                }

                if (pageOptional.isPresent()) {
                    page = (IPage) pageOptional.get();
                    if (page.isSearchCount()) {
                        cacheKey = this.getCountCacheKey(ms, boundSql, parameterObject, RowBounds.DEFAULT);
                        count = (Number) this.tcm.getObject(cache, cacheKey);
                        if (count != null) {
                            page.hitCount(true);
                            return new PageList((List) result, count.longValue());
                        }

                        result = this.delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                        List<E> records = (List) result;
                        this.tcm.putObject(cache, cacheKey, page.getTotal());
                        return records;
                    }

                    return new PageList((List) result, 0L);
                }

                return (List) result;
            }
        }

        return this.delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }

    private MappedStatement buildCountMappedStatement(MappedStatement mappedStatement) {
        return (new Builder(mappedStatement.getConfiguration(), mappedStatement.getId() + "." + "count", mappedStatement.getSqlSource(), SqlCommandType.SELECT)).useCache(true).flushCacheRequired(false).lang(mappedStatement.getLang()).resource(mappedStatement.getResource()).databaseId(mappedStatement.getDatabaseId()).cache(mappedStatement.getCache()).build();
    }

    protected CacheKey getCountCacheKey(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject, RowBounds rowBounds) {
        Configuration configuration = mappedStatement.getConfiguration();
        MappedStatement statement = this.buildCountMappedStatement(mappedStatement);
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(statement.getId());
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        cacheKey.update(boundSql.getSql());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        Iterator var10 = parameterMappings.iterator();

        while (var10.hasNext()) {
            ParameterMapping parameterMapping = (ParameterMapping) var10.next();
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
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }

                cacheKey.update(value);
            }
        }

        if (configuration.getEnvironment() != null) {
            cacheKey.update(configuration.getEnvironment().getId());
        }

        return cacheKey;
    }

    public List<BatchResult> flushStatements() throws SQLException {
        return this.delegate.flushStatements();
    }

    public void commit(boolean required) throws SQLException {
        this.delegate.commit(required);
        this.tcm.commit();
    }

    public void rollback(boolean required) throws SQLException {
        try {
            this.delegate.rollback(required);
        } finally {
            if (required) {
                this.tcm.rollback();
            }

        }

    }

    private void ensureNoOutParams(MappedStatement ms, BoundSql boundSql) {
        if (ms.getStatementType() == StatementType.CALLABLE) {
            Iterator var3 = boundSql.getParameterMappings().iterator();

            while (var3.hasNext()) {
                ParameterMapping parameterMapping = (ParameterMapping) var3.next();
                if (parameterMapping.getMode() != ParameterMode.IN) {
                    throw new ExecutorException("Caching stored procedures with OUT params is not supported.  Please configure useCache=false in " + ms.getId() + " statement.");
                }
            }
        }

    }

    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return this.delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    public boolean isCached(MappedStatement ms, CacheKey key) {
        return this.delegate.isCached(ms, key);
    }

    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        this.delegate.deferLoad(ms, resultObject, property, key, targetType);
    }

    public void clearLocalCache() {
        this.delegate.clearLocalCache();
    }

    private void flushCacheIfRequired(MappedStatement ms) {
        Cache cache = ms.getCache();
        if (cache != null && ms.isFlushCacheRequired()) {
            this.tcm.clear(cache);
        }

    }

    public void setExecutorWrapper(Executor executor) {
        throw new UnsupportedOperationException("This method should not be called");
    }
}
