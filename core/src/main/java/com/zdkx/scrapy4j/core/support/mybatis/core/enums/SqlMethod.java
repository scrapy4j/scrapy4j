package com.zdkx.scrapy4j.core.support.mybatis.core.enums;

public enum SqlMethod {
    CREATE_TABLE("create", "", "create table %s (" +
            "%s VARCHAR(50) NOT NULL" +
            ",%s " +
            "PRIMARY KEY (%s) ) " +
            "\n if not EXISTS %s;"),
    INSERT_ONE("insert", "", "<script>\nINSERT INTO %s %s VALUES %s\n</script>"),
    REPLACE_INTO("replaceInto", "", "<script> replace into %s %s values %s;</script>"),
    RAW_SQL_SELECT_LIST("rawSqlSelectList", "", "<script>${sql}</script>"),
    SELECT_BY_ID("selectById", "根据ID 查询一条数据", "SELECT %s FROM %s WHERE %s=#{%s} "),
    UPDATE_BY_ID("updateById", "根据ID 选择修改数据", "<script>\nUPDATE %s %s WHERE %s=#{%s} \n</script>"),
    UPDATE("update", "根据 whereEntity 条件，更新记录", "<script>\nUPDATE %s %s %s %s\n</script>"),
    SAVE_OR_UPDATE("saveOrUpdate", "insert if not exists,update if exists", "");


    private final String method;
    private final String desc;
    private final String sql;

    SqlMethod(String method, String desc, String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }

    public String getMethod() {
        return method;
    }

    public String getDesc() {
        return desc;
    }

    public String getSql() {
        return sql;
    }
}
