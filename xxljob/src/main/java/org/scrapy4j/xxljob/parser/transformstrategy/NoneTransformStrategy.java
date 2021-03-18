package org.scrapy4j.xxljob.parser.transformstrategy;

import org.scrapy4j.xxljob.parser.JSONPropertyMapper;
import java.util.List;

public class NoneTransformStrategy implements TransformStrategy {

    @Override
    public Object exec(Object propertyValue, List<JSONPropertyMapper> currentRow, List<List<JSONPropertyMapper>> allRows) {
        return propertyValue;
    }
}
