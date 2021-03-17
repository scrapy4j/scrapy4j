package com.victory.scrapy4j.xxljob.support.parser.transformstrategy;

import com.alibaba.fastjson.JSONObject;
import com.victory.scrapy4j.xxljob.support.parser.JSONPropertyMapper;


import java.util.List;

/**
 * JSON对象/数组转字符串
 */
public class JSON2StringTransformStrategy implements TransformStrategy {
    @Override
    public Object exec(Object propertyValue, List<JSONPropertyMapper> currentRow, List<List<JSONPropertyMapper>> allRows) {
       if(propertyValue == null){
            return "";
       }
       return JSONObject.toJSONString(propertyValue);
    }
}
