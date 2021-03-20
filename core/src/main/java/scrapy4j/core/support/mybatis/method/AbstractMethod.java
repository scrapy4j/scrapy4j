package scrapy4j.core.support.mybatis.method;

import scrapy4j.core.support.mybatis.core.metadata.TableInfo;
import scrapy4j.core.support.mybatis.toolkit.Constants;
import scrapy4j.core.support.mybatis.toolkit.SqlScriptUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

/*
copied from mybatis-plus
*/
public abstract class AbstractMethod implements Constants {
    protected Configuration configuration;
    protected LanguageDriver languageDriver;
    protected MapperBuilderAssistant builderAssistant;

    public AbstractMethod() {
    }

    public void inject(MapperBuilderAssistant builderAssistant, String mapperName, Class<?> modelClass, TableInfo tableInfo) {
        this.configuration = builderAssistant.getConfiguration();
        this.builderAssistant = builderAssistant;
        this.languageDriver = configuration.getDefaultScriptingLanguageInstance();
        /* 注入自定义方法 */
        injectMappedStatement(mapperName, modelClass, tableInfo);
    }

    public abstract MappedStatement injectMappedStatement(String mapperName, Class<?> modelClass, TableInfo tableInfo);

    public static String getStatementName(String tableName, String methodName) {
        //TODO 这里statementname还要加所有字段进行hash，不然首次运行后，再加字段就命中不了了
        return getMapperName(tableName) + DOT + methodName;
    }

    public static String getMapperName(String tableName) {
        return AbstractMethod.class.getName() + DOT + tableName + "VirtualMapper";
    }

    protected boolean hasMappedStatement(String mappedStatement) {
        return configuration.hasStatement(mappedStatement, false);
    }

    protected String sqlSelectColumns(TableInfo table, boolean queryWrapper) {
        String selectColumns = "*";
        selectColumns = table.getAllSqlSelect();
        return !queryWrapper ? selectColumns : SqlScriptUtils.convertChoose(String.format("%s != null and %s != null", "ew", "ew.sqlSelect"), SqlScriptUtils.unSafeParam("ew.sqlSelect"), selectColumns);
    }

    protected String sqlSet(boolean logic, boolean ew, TableInfo table, boolean judgeAliasNull, final String alias,
                            final String prefix) {
        String sqlScript = table.getAllSqlSet(logic, prefix);
        if (judgeAliasNull) {
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", alias), true);
        }
        if (ew) {
            sqlScript += NEWLINE;
            sqlScript += SqlScriptUtils.convertIf(SqlScriptUtils.unSafeParam(U_WRAPPER_SQL_SET),
                    String.format("%s != null and %s != null", WRAPPER, U_WRAPPER_SQL_SET), false);
        }
        sqlScript = SqlScriptUtils.convertSet(sqlScript);
        return sqlScript;
    }
}
