package com.victory.scrapy4j.xxljob.support.parser.transformstrategy;


import com.victory.scrapy4j.xxljob.support.parser.JSONPropertyMapper;

import java.util.List;

public interface TransformStrategy {
    /**
     *
     * @param propertyValue 当前property的值
     * @param currentRow 当前行所有property的值
     * @param allRows 所有行所有property的值
     * @return
     */
    Object exec(Object propertyValue, List<JSONPropertyMapper> currentRow, List<List<JSONPropertyMapper>> allRows);
}
