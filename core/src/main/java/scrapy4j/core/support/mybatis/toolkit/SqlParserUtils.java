package scrapy4j.core.support.mybatis.toolkit;

import scrapy4j.core.support.mybatis.core.parser.ISqlParser;
import scrapy4j.core.support.mybatis.core.parser.JsqlParserCountOptimize;
import scrapy4j.core.support.mybatis.core.parser.SqlInfo;
import org.apache.ibatis.reflection.MetaObject;

public class SqlParserUtils {
    private static ISqlParser COUNT_SQL_PARSER = null;

    public SqlParserUtils() {
    }

    public static String getOriginalCountSql(String originalSql) {
        return String.format("SELECT COUNT(1) FROM ( %s ) TOTAL", originalSql);
    }

    public static SqlInfo getOptimizeCountSql(boolean optimizeCountSql, ISqlParser sqlParser, String originalSql) {
        if (!optimizeCountSql) {
            return SqlInfo.newInstance().setSql(getOriginalCountSql(originalSql));
        } else {
            if (null == COUNT_SQL_PARSER) {
                if (null != sqlParser) {
                    COUNT_SQL_PARSER = sqlParser;
                } else {
                    COUNT_SQL_PARSER = new JsqlParserCountOptimize();
                }
            }

            return COUNT_SQL_PARSER.parser((MetaObject)null, originalSql);
        }
    }
}
