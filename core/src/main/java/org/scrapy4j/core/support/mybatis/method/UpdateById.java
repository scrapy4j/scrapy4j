package org.scrapy4j.core.support.mybatis.method;

import org.scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import org.scrapy4j.core.support.mybatis.core.metadata.TableInfo;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;

public class UpdateById extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(String mapperName, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.UPDATE_BY_ID;
        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(),
                sqlSet(false, false, tableInfo, false, "", ""),
                tableInfo.getKeyColumn(), tableInfo.getKeyProperty());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        boolean isSelect = false;
        return builderAssistant.addMappedStatement(
                sqlMethod.getMethod(),
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.UPDATE,
                null,
                null,
                null,
                modelClass,
                null,
                Integer.class,
                null,
                !isSelect,
                isSelect,
                false,
                new NoKeyGenerator(),
                null,
                null,
                configuration.getDatabaseId(),
                languageDriver,
                null
        );
    }
}
