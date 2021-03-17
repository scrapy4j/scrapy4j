package com.victory.scrapy4j.core.support.mybatis.core;

import com.victory.scrapy4j.core.support.mybatis.core.config.GlobalConfig;
import com.victory.scrapy4j.core.support.mybatis.core.executor.MybatisBatchExecutor;
import com.victory.scrapy4j.core.support.mybatis.core.executor.MybatisCachingExecutor;
import com.victory.scrapy4j.core.support.mybatis.core.executor.MybatisReuseExecutor;
import com.victory.scrapy4j.core.support.mybatis.core.executor.MybatisSimpleExecutor;
import com.victory.scrapy4j.core.support.mybatis.toolkit.GlobalConfigUtils;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.Transaction;

public class MybatisConfiguration extends Configuration {
    private static final Log logger = LogFactory.getLog(MybatisConfiguration.class);
    protected final MybatisMapperRegistry mybatisMapperRegistry;
    private GlobalConfig globalConfig;

    public MybatisConfiguration(Environment environment) {
        this();
        this.environment = environment;
    }

    public MybatisConfiguration() {
        this.mybatisMapperRegistry = new MybatisMapperRegistry(this);
        this.globalConfig = GlobalConfigUtils.defaults();
        this.mapUnderscoreToCamelCase = true;
        this.languageRegistry.setDefaultDriverClass(MybatisXMLLanguageDriver.class);
    }

    public void addMappedStatement(MappedStatement ms) {
        logger.debug("addMappedStatement: " + ms.getId());
        if (this.mappedStatements.containsKey(ms.getId())) {
            logger.error("mapper[" + ms.getId() + "] is ignored, because it exists, maybe from xml file");
        } else {
            super.addMappedStatement(ms);
        }
    }

    public MapperRegistry getMapperRegistry() {
        return this.mybatisMapperRegistry;
    }

    public <T> void addMapper(Class<T> type) {
        this.mybatisMapperRegistry.addMapper(type);
    }

    public void addMappers(String packageName, Class<?> superType) {
        this.mybatisMapperRegistry.addMappers(packageName, superType);
    }

    public void addMappers(String packageName) {
        this.mybatisMapperRegistry.addMappers(packageName);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return this.mybatisMapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return this.mybatisMapperRegistry.hasMapper(type);
    }

    public void setDefaultScriptingLanguage(Class<? extends LanguageDriver> driver) {
        if (driver == null) {
            driver = MybatisXMLLanguageDriver.class;
        }

        this.getLanguageRegistry().setDefaultDriverClass(driver);
    }

    public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
        executorType = executorType == null ? this.defaultExecutorType : executorType;
        executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
        Object executor;
        if (ExecutorType.BATCH == executorType) {
            executor = new MybatisBatchExecutor(this, transaction);
        } else if (ExecutorType.REUSE == executorType) {
            executor = new MybatisReuseExecutor(this, transaction);
        } else {
            executor = new MybatisSimpleExecutor(this, transaction);
        }

        if (this.cacheEnabled) {
            executor = new MybatisCachingExecutor((Executor)executor);
        }

        return (Executor)this.interceptorChain.pluginAll(executor);
    }

    public void setGlobalConfig(final GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public GlobalConfig getGlobalConfig() {
        return this.globalConfig;
    }
}
