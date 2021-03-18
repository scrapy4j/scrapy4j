package org.scrapy4j.xxljob.parser.transformstrategy;


import org.scrapy4j.xxljob.parser.JSONPropertyMapper;

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
