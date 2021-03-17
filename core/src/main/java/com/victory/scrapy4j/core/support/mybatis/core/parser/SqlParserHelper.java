package com.victory.scrapy4j.core.support.mybatis.core.parser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.victory.scrapy4j.core.support.mybatis.annotation.SqlParser;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;

public class SqlParserHelper {
    public static final String DELEGATE_MAPPED_STATEMENT = "delegate.mappedStatement";
    private static final Map<String, Boolean> SQL_PARSER_INFO_CACHE = new ConcurrentHashMap();

    public SqlParserHelper() {
    }

    public static synchronized void initSqlParserInfoCache(Class<?> mapperClass) {
        SqlParser sqlParser = (SqlParser)mapperClass.getAnnotation(SqlParser.class);
        if (sqlParser != null) {
            SQL_PARSER_INFO_CACHE.put(mapperClass.getName(), sqlParser.filter());
        }

    }

    public static void initSqlParserInfoCache(String mapperClassName, Method method) {
        SqlParser sqlParser = (SqlParser)method.getAnnotation(SqlParser.class);
        if (sqlParser != null) {
            if (SQL_PARSER_INFO_CACHE.containsKey(mapperClassName)) {
                Boolean value = (Boolean)SQL_PARSER_INFO_CACHE.get(mapperClassName);
                if (!value.equals(sqlParser.filter())) {
                    String sid = mapperClassName + "." + method.getName();
                    SQL_PARSER_INFO_CACHE.putIfAbsent(sid, sqlParser.filter());
                }
            } else {
                String sid = mapperClassName + "." + method.getName();
                SQL_PARSER_INFO_CACHE.putIfAbsent(sid, sqlParser.filter());
            }
        }

    }

    public static boolean getSqlParserInfo(MetaObject metaObject) {
        String id = getMappedStatement(metaObject).getId();
        Boolean value = (Boolean)SQL_PARSER_INFO_CACHE.get(id);
        if (value != null) {
            return value;
        } else {
            String mapperName = id.substring(0, id.lastIndexOf("."));
            return (Boolean)SQL_PARSER_INFO_CACHE.getOrDefault(mapperName, false);
        }
    }

    public static MappedStatement getMappedStatement(MetaObject metaObject) {
        return (MappedStatement)metaObject.getValue("delegate.mappedStatement");
    }
}
