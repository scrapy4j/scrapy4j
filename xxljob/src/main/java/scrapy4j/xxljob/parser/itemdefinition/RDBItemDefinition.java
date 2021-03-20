package scrapy4j.xxljob.parser.itemdefinition;

import scrapy4j.core.item.Item;
import scrapy4j.core.item.RDBItem;
import scrapy4j.core.pojo.Response;
import scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import scrapy4j.core.support.mybatis.core.metadata.TableFieldInfo;
import scrapy4j.core.utils.Utils;
import scrapy4j.core.exceptions.IgnoreRowException;
import scrapy4j.xxljob.parser.JSONPropertyMapper;
import scrapy4j.xxljob.parser.transformstrategy.NoneTransformStrategy;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RDBItemDefinition implements ItemDefinition {
    private String tableName;
    private List<JSONPropertyMapper> propertyMappings;
    private JSONPropertyMapper idMapping;
    private SqlMethod sqlMethod = SqlMethod.SAVE_OR_UPDATE;

    public RDBItemDefinition() {

    }

    public RDBItemDefinition(List<JSONPropertyMapper> propertyMappings, String tableName) {
        List<JSONPropertyMapper> idMappings = propertyMappings.stream().filter(JSONPropertyMapper::isPrimaryKey).collect(Collectors.toList());
        if (idMappings.size() != 1) {
            throw new RuntimeException("primaryKey must be announced and only one");
        }
        this.idMapping = idMappings.get(0);
        this.propertyMappings = propertyMappings;
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<JSONPropertyMapper> getPropertyMappings() {
        return propertyMappings;
    }

    public void setPropertyMappings(List<JSONPropertyMapper> propertyMappings) {
        this.propertyMappings = propertyMappings;
    }

    public JSONPropertyMapper getIdMapping() {
        return idMapping;
    }

    public void setIdMapping(JSONPropertyMapper idMapping) {
        this.idMapping = idMapping;
    }

    public SqlMethod getSqlMethod() {
        return sqlMethod;
    }

    public void setSqlMethod(SqlMethod sqlMethod) {
        this.sqlMethod = sqlMethod;
    }

    @Override
    public List<Item> extractAndTransformItems(Response response) {
        Map<String, List<Object>> propertyValueList = new HashMap<>();
        AtomicInteger maxRowLength = new AtomicInteger();
        this.propertyMappings.forEach(m -> {
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
                // 需要进行取值的property，如果设置了固定值则不需要通过propertyExpression去取值
                if (mapperBuilder.getPropertyValue() == null) {
                    mapperBuilder.propertyValue(propertyValueList.get(m.getPropertyName()).get(finalI));
                } else {
                    if (Utils.isMustacheVariable(m.getPropertyValue().toString())) {
                        mapperBuilder.propertyValue(Utils.formatVars(mapperBuilder.getPropertyValue().toString(), response.getRequest().getResolvedVariables()));
                    }
                }
                rowPropertyMapper.add(mapperBuilder.build());
            });
            rowList.add(rowPropertyMapper);
        }

        // newRowList存储修改后的数据, 方便遍历过程对数组内容进行修改
        List<List<JSONPropertyMapper>> newRowList = new ArrayList<>();
        for (int i = 0; i < rowList.size(); i++) {
            List<JSONPropertyMapper> currentRow = rowList.get(i);
            List<JSONPropertyMapper> newCurrentRow = new ArrayList<>(currentRow);
            Iterator<JSONPropertyMapper> it = currentRow.iterator();
            try {
                while (it.hasNext()){
                    JSONPropertyMapper valueMapping = it.next();
                    if (valueMapping.getTransformStrategy() == null) {
                        valueMapping.setTransformStrategy(new NoneTransformStrategy());
                    }
                    Object columnValue = valueMapping.getTransformStrategy().exec(valueMapping.getPropertyValue(), newCurrentRow, rowList);
                    valueMapping.setColumnValue(columnValue);//transform
                }

                newRowList.add(newCurrentRow);
            } catch (IgnoreRowException ex) {
                Utils.logError(response.getRequest().getSpider().getSettings().getLogger(RDBItemDefinition.class), String.format("transform error and remove row %s", ex.getMessage()), ex);
            } catch (RuntimeException ex) {
                newRowList.add(newCurrentRow);
                Utils.logError(response.getRequest().getSpider().getSettings().getLogger(RDBItemDefinition.class), String.format("transform error %s",  ex.getMessage()), ex);
            }
        }
        rowList = newRowList;

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
            item.setTableName(this.tableName);
            item.setKeyProperty(this.idMapping.getPropertyName());
            item.setKeyColumn(this.idMapping.getColumnName());
            item.setIdType(this.idMapping.getIdType());
            item.setValues(map);
            item.setSqlMethod(this.sqlMethod);
            itemList.add(item);
        });
        return itemList;
    }
}
