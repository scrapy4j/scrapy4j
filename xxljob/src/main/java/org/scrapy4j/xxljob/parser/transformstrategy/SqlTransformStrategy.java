package org.scrapy4j.xxljob.parser.transformstrategy;
import org.scrapy4j.xxljob.parser.JSONPropertyMapper;

import java.util.List;

public class SqlTransformStrategy implements TransformStrategy {
    private String sql;

    SqlTransformStrategy(String sql){
        this.sql = sql;
    }

    @Override
    public Object exec(Object propertyValue, List<JSONPropertyMapper> currentRow, List<List<JSONPropertyMapper>> allRows) {
        return null;
    }
}
