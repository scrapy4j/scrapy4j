package scrapy4j.core.support.mybatis.core.parser;

import org.apache.ibatis.reflection.MetaObject;

public interface ISqlParser {
    SqlInfo parser(MetaObject metaObject, String sql);

    default boolean doFilter(final MetaObject metaObject, final String sql) {
        return true;
    }
}