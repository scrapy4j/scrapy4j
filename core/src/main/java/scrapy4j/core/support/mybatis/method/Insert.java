package scrapy4j.core.support.mybatis.method;


import scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import scrapy4j.core.support.mybatis.core.metadata.TableInfo;
import scrapy4j.core.support.mybatis.core.enums.IdType;
import scrapy4j.core.support.mybatis.toolkit.StringUtils;
import scrapy4j.core.support.mybatis.toolkit.SqlScriptUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import scrapy4j.core.support.mybatis.toolkit.StringPool;

/*
copied from mybatis-plus
*/
public class Insert extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(String mapperName, Class<?> modelClass, TableInfo tableInfo) {
        KeyGenerator keyGenerator = new NoKeyGenerator();
        SqlMethod sqlMethod = SqlMethod.INSERT_ONE;
        String columnScript = SqlScriptUtils.convertTrim(
                tableInfo.getAllInsertSqlColumnMaybeIf(),
                StringPool.LEFT_BRACKET,
                StringPool.RIGHT_BRACKET,
                null,
                StringPool.COMMA
        );
        String valuesScript = SqlScriptUtils.convertTrim(
                tableInfo.getAllInsertSqlPropertyMaybeIf(null),
                StringPool.LEFT_BRACKET,
                StringPool.RIGHT_BRACKET,
                null,
                StringPool.COMMA
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
                SqlCommandType.INSERT,
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
