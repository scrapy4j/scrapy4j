package scrapy4j.core.support.mybatis.method;

import scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import scrapy4j.core.support.mybatis.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.scripting.defaults.RawSqlSource;

import java.util.Map;

public class SelectById extends AbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(String mapperName, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.SELECT_BY_ID;
        SqlSource sqlSource = new RawSqlSource(configuration, String.format(sqlMethod.getSql(),
                sqlSelectColumns(tableInfo, false),
                tableInfo.getTableName(), tableInfo.getKeyColumn(), tableInfo.getKeyProperty()), Object.class);
        boolean isSelect = true;
        if (hasMappedStatement(super.getStatementName(tableInfo.getTableName(), sqlMethod.getMethod()))) {
            return null;
        }
        return builderAssistant.addMappedStatement(
                sqlMethod.getMethod(),
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.SELECT,
                null,
                null,
                null,
                modelClass,
                null,
                Map.class,
                null,
                !isSelect,
                isSelect,
                false,
                null,
                null,
                null,
                configuration.getDatabaseId(),
                languageDriver,
                null
        );
    }
}
