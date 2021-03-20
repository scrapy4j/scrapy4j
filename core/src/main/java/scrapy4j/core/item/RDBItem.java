package scrapy4j.core.item;

import scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import scrapy4j.core.support.mybatis.core.metadata.TableFieldInfo;
import scrapy4j.core.support.mybatis.core.metadata.TableInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * relational database item
 */
public class RDBItem<T> extends TableInfo implements Item<T> {
    public RDBItem() {
    }

    private T values;

    private SqlMethod sqlMethod;

    public SqlMethod getSqlMethod() {
        return sqlMethod;
    }

    public void setSqlMethod(SqlMethod sqlMethod) {
        this.sqlMethod = sqlMethod;
    }

    @Override
    public void setValues(T object) {
        this.values = object;
    }

    @Override
    public T values() {
        return values;
    }

    public void addIdField(String property, String column) {
        this.setKeyColumn(column);
        this.setKeyProperty(property);
    }

    public void addIdField(String property) {
        this.setKeyColumn(property);
        this.setKeyProperty(property);
    }


    public void addField(TableFieldInfo tableFieldInfo) {
        if (fieldList == null) {
            fieldList = new ArrayList<>();
        }
        if (StringUtils.isBlank(tableFieldInfo.getColumn())) {
            tableFieldInfo.setColumn(tableFieldInfo.getProperty());
        }
        fieldList.add(new TableFieldInfo(
                        this,
                        tableFieldInfo.getProperty(),
                        tableFieldInfo.getColumn(),
                        tableFieldInfo.getInsertStrategy(),
                        tableFieldInfo.getUpdateStrategy()
                )
        );
    }
}
