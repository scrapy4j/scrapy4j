package com.victory.scrapy4j.xxljob.support.parser;

import com.victory.scrapy4j.core.component.item.Item;
import com.victory.scrapy4j.core.component.item.RDBItem;
import com.victory.scrapy4j.core.component.parser.Parser;
import com.victory.scrapy4j.core.component.pojo.Response;
import com.victory.scrapy4j.core.component.pojo.Result;
import com.victory.scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import com.victory.scrapy4j.core.support.mybatis.core.metadata.TableFieldInfo;
import com.victory.scrapy4j.core.utils.Utils;
import com.victory.scrapy4j.xxljob.support.parser.interceptor.Interceptor;
import com.victory.scrapy4j.xxljob.support.parser.interceptor.InterceptorChain;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * JSON格式数据解析处理类
 */
@Deprecated
public class JSONPropertyRDBItemParser implements Parser {

    private final String tableName;
    private final List<JSONPropertyMapper> propertyMappings;
    private final JSONPropertyMapper idMapping;

    private InterceptorChain interceptorChain = new InterceptorChain();

    public static JSONPropertyRDBItemParserBuilder builder(List<JSONPropertyMapper> propertyMappings, String tableName) {
        return new JSONPropertyRDBItemParserBuilder(propertyMappings, tableName);
    }

    private JSONPropertyRDBItemParser(JSONPropertyRDBItemParserBuilder builder) {
        this.tableName = builder.tableName;
        this.propertyMappings = builder.propertyMappings;
        this.idMapping = builder.idMapping;
        this.interceptorChain = builder.interceptorChain;
    }

    @Override
    public Result parse(Response response) {
        Result result = new Result();

        //pre handle
        if (!interceptorChain.applyPreHandle(response.getRequest(), response, result)) {
            return result;
        }

        //extract&transform
        Map<String, List<Object>> propertyValueList = new HashMap<>();
        AtomicInteger maxRowLength = new AtomicInteger();
        propertyMappings.forEach(m -> {
            if (!StringUtils.isEmpty(m.getPropertyExpression())) {//需要取值的情况
                List<Object> values = response.json(m.getPropertyExpression()).getAll();
                if (values.size() > maxRowLength.get()) {
                    maxRowLength.set(values.size());
                }
                propertyValueList.put(m.getPropertyName(), values);
            }
        });

        // 构建映射数组
        List<List<JSONPropertyMapper>> rowList = new ArrayList<>();
        for (int i = 0; i < maxRowLength.get(); i++) {
            List<JSONPropertyMapper> rowPropertyMapper = new ArrayList<>();
            int finalI = i;
            propertyMappings.forEach(m -> {
                JSONPropertyMapper.JSONPropertyMapperBuilder mapperBuilder = JSONPropertyMapper.builder().clone(m);
                //需要进行取值的property，如果设置了固定值则不需要通过propertyExpression去取值
                if (mapperBuilder.getPropertyValue() == null) {
                    mapperBuilder.propertyValue(propertyValueList.get(m.getPropertyName()).get(finalI));
                } else {
                    if(Utils.isMustacheVariable(m.getPropertyValue().toString())) {
                        mapperBuilder.propertyValue(Utils.formatVars(mapperBuilder.getPropertyValue().toString(), response.getRequest().getVariables()));
                    }
                }
                rowPropertyMapper.add(mapperBuilder.build());
            });
            rowList.add(rowPropertyMapper);
        }
        rowList.forEach(list -> {
            list.forEach(valueMapping -> {
                valueMapping.setColumnValue(valueMapping.getTransformStrategy().exec(valueMapping.getPropertyValue(), list, rowList));//transform
            });
        });

        // 构建item
        List<Item> itemList = new ArrayList<>();
        rowList.forEach(row -> {
            RDBItem item = new RDBItem();
            Map<String, Object> map = new HashMap<>();
            row.forEach(m -> {
                map.put(m.getPropertyName(), m.getColumnValue());
                if (!m.isPrimaryKey()) {
                    item.addField(new TableFieldInfo(m.getPropertyName(), m.getColumnName(), m.getInsertStrategy(), m.getUpdateStrategy()));
                }
            });
            item.setTableName(tableName);
            item.setKeyProperty(idMapping.getPropertyName());
            item.setKeyColumn(idMapping.getColumnName());
            item.setIdType(idMapping.getIdType());
            item.setValues(map);
            item.setSqlMethod(SqlMethod.SAVE_OR_UPDATE);
            itemList.add(item);
        });

        result.setItems(itemList);
        //post handle
        interceptorChain.applyPostHandle(response.getRequest(), response, result);

        return result;
    }

    @Deprecated
    public static final class JSONPropertyRDBItemParserBuilder {
        private JSONPropertyMapper idMapping;
        private List<JSONPropertyMapper> propertyMappings;
        private String tableName;
        private InterceptorChain interceptorChain = new InterceptorChain();

        private JSONPropertyRDBItemParserBuilder() {
        }

        public JSONPropertyRDBItemParserBuilder(List<JSONPropertyMapper> propertyMappings, String tableName) {
            List<JSONPropertyMapper> idMappings = propertyMappings.stream().filter(JSONPropertyMapper::isPrimaryKey).collect(Collectors.toList());
            if (idMappings.size() != 1) {
                throw new RuntimeException("primaryKey must be announced and only one");
            }
            this.idMapping = idMappings.get(0);
            this.propertyMappings = propertyMappings;
            this.tableName = tableName;
        }

        public JSONPropertyRDBItemParserBuilder interceptor(Interceptor interceptor) {
            this.interceptorChain.addInterceptor(interceptor);
            return this;
        }

        public JSONPropertyRDBItemParserBuilder interceptors(List<Interceptor> interceptors) {
            this.interceptorChain.addInterceptors(interceptors);
            return this;
        }

        public JSONPropertyRDBItemParser build() {
            return new JSONPropertyRDBItemParser(this);
        }
    }
}
