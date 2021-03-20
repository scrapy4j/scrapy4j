package scrapy4j.xxljob.parser.transformstrategy;


import scrapy4j.xxljob.parser.JSONPropertyMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 默认值翻译策略
 */
public class DefaultValueTransformStrategy implements TransformStrategy {
    private String newVal = "";

    public DefaultValueTransformStrategy(String newVal) {
        this.newVal = newVal;
    }

    public DefaultValueTransformStrategy() {
    }

    @Override
    public Object exec(Object propertyValue, List<JSONPropertyMapper> currentRow, List<List<JSONPropertyMapper>> allRows) {
       if(StringUtils.isNotBlank(newVal)){
           return newVal;
       }else{
           return propertyValue;
       }
    }
}
