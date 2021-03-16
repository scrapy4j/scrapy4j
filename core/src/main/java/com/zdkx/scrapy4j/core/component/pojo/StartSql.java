package com.zdkx.scrapy4j.core.component.pojo;

import java.util.Map;

public class StartSql {
    private String sql;
    private Map<String, String> sqlParameters;
    private Integer pageSize;

    public StartSql(String sql) {
        this.sql = sql;
    }

    public StartSql() {
    }

    public String getSql() {
        return sql;
    }

    public Map<String, String> getSqlParameters() {
        return sqlParameters;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public static StartSqlBuilder builder() {
        return new StartSqlBuilder();
    }

    public static final class StartSqlBuilder extends StartSql {
        private String sql;
        private Map<String, String> sqlParameters;
        private Integer pageSize;

        public StartSqlBuilder() {
        }

        public StartSqlBuilder sql(String sql) {
            this.sql = sql;
            return this;
        }

        public StartSqlBuilder sqlParameters(Map<String, String> sqlParameters) {
            this.sqlParameters = sqlParameters;
            return this;
        }

        public StartSqlBuilder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public StartSql build() {
            StartSql startSql = new StartSql(sql);
            startSql.sqlParameters = this.sqlParameters;
            startSql.pageSize = this.pageSize;
            return startSql;
        }
    }
}
