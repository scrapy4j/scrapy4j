package com.zdkx.scrapy4j.core.support.mybatis.core.config;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.zdkx.scrapy4j.core.support.mybatis.annotation.FieldStrategy;
import com.zdkx.scrapy4j.core.support.mybatis.annotation.IdType;
import com.zdkx.scrapy4j.core.support.mybatis.core.incrementer.IKeyGenerator;
import com.zdkx.scrapy4j.core.support.mybatis.core.incrementer.IdentifierGenerator;
import com.zdkx.scrapy4j.core.support.mybatis.core.injector.DefaultSqlInjector;
import com.zdkx.scrapy4j.core.support.mybatis.core.injector.ISqlInjector;
import com.zdkx.scrapy4j.core.support.mybatis.core.mapper.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;

public class GlobalConfig implements Serializable {
    private boolean banner = true;
    /** @deprecated */
    @Deprecated
    private Long workerId;
    /** @deprecated */
    @Deprecated
    private Long datacenterId;
    private boolean enableSqlRunner = false;
    private GlobalConfig.DbConfig dbConfig;
    private ISqlInjector sqlInjector = new DefaultSqlInjector();
    private Class<?> superMapperClass = Mapper.class;
    private SqlSessionFactory sqlSessionFactory;
    private Set<String> mapperRegistryCache = new ConcurrentSkipListSet();
//    private MetaObjectHandler metaObjectHandler;
    private IdentifierGenerator identifierGenerator;

    public GlobalConfig() {
    }

    public boolean isBanner() {
        return this.banner;
    }

    /** @deprecated */
    @Deprecated
    public Long getWorkerId() {
        return this.workerId;
    }

    /** @deprecated */
    @Deprecated
    public Long getDatacenterId() {
        return this.datacenterId;
    }

    public boolean isEnableSqlRunner() {
        return this.enableSqlRunner;
    }

    public GlobalConfig.DbConfig getDbConfig() {
        return this.dbConfig;
    }

    public ISqlInjector getSqlInjector() {
        return this.sqlInjector;
    }

    public Class<?> getSuperMapperClass() {
        return this.superMapperClass;
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return this.sqlSessionFactory;
    }

    public Set<String> getMapperRegistryCache() {
        return this.mapperRegistryCache;
    }

//    public MetaObjectHandler getMetaObjectHandler() {
//        return this.metaObjectHandler;
//    }

    public IdentifierGenerator getIdentifierGenerator() {
        return this.identifierGenerator;
    }

    public GlobalConfig setBanner(final boolean banner) {
        this.banner = banner;
        return this;
    }

    /** @deprecated */
    @Deprecated
    public GlobalConfig setWorkerId(final Long workerId) {
        this.workerId = workerId;
        return this;
    }

    /** @deprecated */
    @Deprecated
    public GlobalConfig setDatacenterId(final Long datacenterId) {
        this.datacenterId = datacenterId;
        return this;
    }

    public GlobalConfig setEnableSqlRunner(final boolean enableSqlRunner) {
        this.enableSqlRunner = enableSqlRunner;
        return this;
    }

    public GlobalConfig setDbConfig(final GlobalConfig.DbConfig dbConfig) {
        this.dbConfig = dbConfig;
        return this;
    }

    public GlobalConfig setSqlInjector(final ISqlInjector sqlInjector) {
        this.sqlInjector = sqlInjector;
        return this;
    }

    public GlobalConfig setSuperMapperClass(final Class<?> superMapperClass) {
        this.superMapperClass = superMapperClass;
        return this;
    }

    public GlobalConfig setSqlSessionFactory(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
        return this;
    }

    public GlobalConfig setMapperRegistryCache(final Set<String> mapperRegistryCache) {
        this.mapperRegistryCache = mapperRegistryCache;
        return this;
    }

//    public GlobalConfig setMetaObjectHandler(final MetaObjectHandler metaObjectHandler) {
//        this.metaObjectHandler = metaObjectHandler;
//        return this;
//    }

    public GlobalConfig setIdentifierGenerator(final IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
        return this;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof GlobalConfig;
    }

    public static class DbConfig {
        private IdType idType;
        private String tablePrefix;
        private String schema;
        private String columnFormat;
        private String propertyFormat;
        private boolean tableUnderline;
        private boolean capitalMode;
        private IKeyGenerator keyGenerator;
        private String logicDeleteField;
        private String logicDeleteValue;
        private String logicNotDeleteValue;
        private FieldStrategy insertStrategy;
        private FieldStrategy updateStrategy;
        private FieldStrategy selectStrategy;

        public DbConfig() {
            this.idType = IdType.ASSIGN_ID;
            this.tableUnderline = true;
            this.capitalMode = false;
            this.logicDeleteValue = "1";
            this.logicNotDeleteValue = "0";
            this.insertStrategy = FieldStrategy.NOT_NULL;
            this.updateStrategy = FieldStrategy.NOT_NULL;
            this.selectStrategy = FieldStrategy.NOT_NULL;
        }

        public IdType getIdType() {
            return this.idType;
        }

        public String getTablePrefix() {
            return this.tablePrefix;
        }

        public String getSchema() {
            return this.schema;
        }

        public String getColumnFormat() {
            return this.columnFormat;
        }

        public String getPropertyFormat() {
            return this.propertyFormat;
        }

        public boolean isTableUnderline() {
            return this.tableUnderline;
        }

        public boolean isCapitalMode() {
            return this.capitalMode;
        }

        public IKeyGenerator getKeyGenerator() {
            return this.keyGenerator;
        }

        public String getLogicDeleteField() {
            return this.logicDeleteField;
        }

        public String getLogicDeleteValue() {
            return this.logicDeleteValue;
        }

        public String getLogicNotDeleteValue() {
            return this.logicNotDeleteValue;
        }

        public FieldStrategy getInsertStrategy() {
            return this.insertStrategy;
        }

        public FieldStrategy getUpdateStrategy() {
            return this.updateStrategy;
        }

        public FieldStrategy getSelectStrategy() {
            return this.selectStrategy;
        }

        public GlobalConfig.DbConfig setIdType(final IdType idType) {
            this.idType = idType;
            return this;
        }

        public GlobalConfig.DbConfig setTablePrefix(final String tablePrefix) {
            this.tablePrefix = tablePrefix;
            return this;
        }

        public GlobalConfig.DbConfig setSchema(final String schema) {
            this.schema = schema;
            return this;
        }

        public GlobalConfig.DbConfig setColumnFormat(final String columnFormat) {
            this.columnFormat = columnFormat;
            return this;
        }

        public GlobalConfig.DbConfig setPropertyFormat(final String propertyFormat) {
            this.propertyFormat = propertyFormat;
            return this;
        }

        public GlobalConfig.DbConfig setTableUnderline(final boolean tableUnderline) {
            this.tableUnderline = tableUnderline;
            return this;
        }

        public GlobalConfig.DbConfig setCapitalMode(final boolean capitalMode) {
            this.capitalMode = capitalMode;
            return this;
        }

        public GlobalConfig.DbConfig setKeyGenerator(final IKeyGenerator keyGenerator) {
            this.keyGenerator = keyGenerator;
            return this;
        }

        public GlobalConfig.DbConfig setLogicDeleteField(final String logicDeleteField) {
            this.logicDeleteField = logicDeleteField;
            return this;
        }

        public GlobalConfig.DbConfig setLogicDeleteValue(final String logicDeleteValue) {
            this.logicDeleteValue = logicDeleteValue;
            return this;
        }

        public GlobalConfig.DbConfig setLogicNotDeleteValue(final String logicNotDeleteValue) {
            this.logicNotDeleteValue = logicNotDeleteValue;
            return this;
        }

        public GlobalConfig.DbConfig setInsertStrategy(final FieldStrategy insertStrategy) {
            this.insertStrategy = insertStrategy;
            return this;
        }

        public GlobalConfig.DbConfig setUpdateStrategy(final FieldStrategy updateStrategy) {
            this.updateStrategy = updateStrategy;
            return this;
        }

        public GlobalConfig.DbConfig setSelectStrategy(final FieldStrategy selectStrategy) {
            this.selectStrategy = selectStrategy;
            return this;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof GlobalConfig.DbConfig;
        }

        public String toString() {
            return "GlobalConfig.DbConfig(idType=" + this.getIdType() + ", tablePrefix=" + this.getTablePrefix() + ", schema=" + this.getSchema() + ", columnFormat=" + this.getColumnFormat() + ", propertyFormat=" + this.getPropertyFormat() + ", tableUnderline=" + this.isTableUnderline() + ", capitalMode=" + this.isCapitalMode() + ", keyGenerator=" + this.getKeyGenerator() + ", logicDeleteField=" + this.getLogicDeleteField() + ", logicDeleteValue=" + this.getLogicDeleteValue() + ", logicNotDeleteValue=" + this.getLogicNotDeleteValue() + ", insertStrategy=" + this.getInsertStrategy() + ", updateStrategy=" + this.getUpdateStrategy() + ", selectStrategy=" + this.getSelectStrategy() + ")";
        }
    }
}
