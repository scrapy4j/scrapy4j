package com.victory.scrapy4j.xxljob.support.parser.transformstrategy;
import com.victory.scrapy4j.xxljob.support.parser.JSONPropertyMapper;

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
