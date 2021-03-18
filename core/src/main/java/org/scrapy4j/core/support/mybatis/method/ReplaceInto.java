package org.scrapy4j.core.support.mybatis.method;

import org.scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import org.scrapy4j.core.support.mybatis.core.metadata.TableInfo;
import org.scrapy4j.core.support.mybatis.core.enums.IdType;
import org.scrapy4j.core.support.mybatis.toolkit.StringUtils;
import org.scrapy4j.core.support.mybatis.toolkit.SqlScriptUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;

public class ReplaceInto extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(String mapperName, Class<?> modelClass, TableInfo tableInfo) {
        KeyGenerator keyGenerator = new NoKeyGenerator();
        SqlMethod sqlMethod = SqlMethod.REPLACE_INTO;
        String columnScript = SqlScriptUtils.convertTrim(
                tableInfo.getAllInsertSqlColumnMaybeIf(),
                LEFT_BRACKET,
                RIGHT_BRACKET,
                null,
                COMMA
        );
        String valuesScript = SqlScriptUtils.convertTrim(
                tableInfo.getAllInsertSqlPropertyMaybeIf(null),
                LEFT_BRACKET,
                RIGHT_BRACKET,
                null,
                COMMA
        );
        String keyProperty = null;
        String keyColumn = null;

        // 表包含主键处理逻辑,如果不包含主键当普通字段处理
        if (StringUtils.isNotBlank(tableInfo.getKeyProperty())) {
            if (tableInfo.getIdType() == IdType.AUTO) {
                /** 自增主键 */
                keyGenerator = new Jdbc3KeyGenerator();
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = tableInfo.getKeyColumn();
            } else {
            }
        }
        String sql = String.format(
                sqlMethod.getSql(),
                tableInfo.getTableName(),
                columnScript,
                valuesScript
        );
        SqlSource sqlSource = languageDriver.createSqlSource(
                configuration,
                sql,
                modelClass
        );
        if (hasMappedStatement(super.getStatementName(tableInfo.getTableName(), sqlMethod.getMethod()))) {
            return null;
        }
        /* 缓存逻辑处理 */
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
                keyGenerator,
                keyProperty,
                keyColumn,
                configuration.getDatabaseId(),
                languageDriver,
                null
        );
    }

}
