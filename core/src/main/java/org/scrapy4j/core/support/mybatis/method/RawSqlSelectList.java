package org.scrapy4j.core.support.mybatis.method;

import org.scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import org.scrapy4j.core.support.mybatis.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;

import java.util.Map;

public class RawSqlSelectList extends AbstractMethod{

    @Override
    public MappedStatement injectMappedStatement(String mapperName, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.RAW_SQL_SELECT_LIST;
        String sql = String.format(sqlMethod.getSql());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        if (hasMappedStatement(super.getStatementName(tableInfo.getTableName(),sqlMethod.getMethod()))) {
            return null;
        }
        boolean isSelect = false;
        return builderAssistant.addMappedStatement(sqlMethod.getMethod(), sqlSource, StatementType.PREPARED, SqlCommandType.UPDATE,
                null, null, null, modelClass, null, Map.class,
                null, !isSelect, isSelect, false, null, null, null,
                configuration.getDatabaseId(), languageDriver, null);
    }
}