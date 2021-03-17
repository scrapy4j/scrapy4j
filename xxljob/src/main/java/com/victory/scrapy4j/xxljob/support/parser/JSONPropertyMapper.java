package com.victory.scrapy4j.xxljob.support.parser;

import cn.hutool.core.lang.Assert;
import com.victory.scrapy4j.core.support.mybatis.annotation.FieldStrategy;
import com.victory.scrapy4j.core.support.mybatis.annotation.IdType;
import com.victory.scrapy4j.xxljob.support.parser.transformstrategy.NoneTransformStrategy;
import com.victory.scrapy4j.xxljob.support.parser.transformstrategy.TransformStrategy;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class JSONPropertyMapper implements Serializable {
    /**
     * 举例:
     * [*].id 表示json根节点是集合，从数组里取每个对象的属性
     * category[*].id 表示json根节点是对象，有一个category的集合
     * data.id 表示json根节点是对象
     */
    private String propertyExpression;

    /**
     * 属性名称
     */
    private String propertyName;

    /**
     * 属性值
     */
    private Object propertyValue;

    /**
     * 翻译Strategy
     */
    private TransformStrategy transformStrategy;

    /**
     * 主键
     */
    private Boolean primaryKey;

    /**
     * 主键类型
     */
    private IdType idType;

    /**
     * 入库参数名
     */
    private String columnName;

    /**
     * 入库参数值
     */
    private Object columnValue;

    /**
     * 插入策略
     */
    private FieldStrategy insertStrategy;

    /**
     * 更新策略
     */
    private FieldStrategy updateStrategy;

    public JSONPropertyMapperBuilder toBuilder() {
        return new JSONPropertyMapperBuilder(
                this.propertyExpression,
                this.primaryKey,
                this.idType,
                this.columnName,
                this.transformStrategy,
                this.insertStrategy,
                this.updateStrategy)
                .propertyName(this.propertyName)
                .propertyValue(this.propertyValue);
    }

    public JSONPropertyMapper(JSONPropertyMapperBuilder builder) {
        this.propertyExpression = builder.propertyExpression;
        this.primaryKey = builder.primaryKey;
        this.idType = builder.idType;
        this.columnName = builder.columnName;
        this.transformStrategy = builder.transformStrategy;
        this.insertStrategy = builder.insertStrategy;
        this.updateStrategy = builder.updateStrategy;
        this.propertyName = builder.propertyName;
        this.propertyValue = builder.propertyValue;
    }

    public static JSONPropertyMapperBuilder builder() {
        return new JSONPropertyMapperBuilder();
    }

    public static JSONPropertyMapperBuilder builder(String propertyExpression, String columnName) {
        return new JSONPropertyMapperBuilder(propertyExpression, false, null, columnName, new NoneTransformStrategy());
    }

    public static JSONPropertyMapperBuilder builder(String propertyExpression, String columnName, TransformStrategy transformStrategy) {
        return new JSONPropertyMapperBuilder(propertyExpression, false, null, columnName, transformStrategy, null);
    }

    public static JSONPropertyMapperBuilder builder(String propertyExpression, String columnName, TransformStrategy transformStrategy, FieldStrategy insertStrategy) {
        return new JSONPropertyMapperBuilder(propertyExpression, false, null, columnName, transformStrategy, insertStrategy);
    }

    public static JSONPropertyMapperBuilder builder(String propertyExpression, String columnName, TransformStrategy transformStrategy, FieldStrategy insertStrategy, FieldStrategy updateStrategy) {
        return new JSONPropertyMapperBuilder(propertyExpression, false, null, columnName, transformStrategy, insertStrategy, updateStrategy);
    }


    public static JSONPropertyMapperBuilder builder(String propertyExpression, Boolean primaryKey, IdType idType, String columnName) {
        return new JSONPropertyMapperBuilder(propertyExpression, primaryKey, idType, columnName, new NoneTransformStrategy());
    }

    public static JSONPropertyMapperBuilder builder(String propertyExpression, Boolean primaryKey, IdType idType, String columnName, TransformStrategy transformStrategy) {
        return new JSONPropertyMapperBuilder(propertyExpression, primaryKey, idType, columnName, transformStrategy, null);
    }

    public static JSONPropertyMapperBuilder builder(String propertyExpression, Boolean primaryKey, IdType idType, String columnName, TransformStrategy transformStrategy, FieldStrategy insertStrategy) {
        return new JSONPropertyMapperBuilder(propertyExpression, primaryKey, idType, columnName, transformStrategy, insertStrategy, null);
    }

    public static JSONPropertyMapperBuilder builder(String propertyExpression, Boolean primaryKey, IdType idType, String columnName, TransformStrategy transformStrategy, FieldStrategy insertStrategy, FieldStrategy updateStrategy) {
        return new JSONPropertyMapperBuilder(propertyExpression, primaryKey, idType, columnName, transformStrategy, insertStrategy, updateStrategy);
    }

    public void setPropertyValue(Object propertyValue) {
        this.propertyValue = propertyValue;
    }

    public void setColumnValue(Object columnValue) {
        this.columnValue = columnValue;
    }

    public String getPropertyExpression() {
        return propertyExpression;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    public TransformStrategy getTransformStrategy() {
        return transformStrategy;
    }

    public void setTransformStrategy(TransformStrategy transformStrategy) {
        this.transformStrategy = transformStrategy;
    }

    public Boolean isPrimaryKey() {
        return primaryKey != null && primaryKey;
    }

    public IdType getIdType() {
        return idType;
    }

    public String getColumnName() {
        return columnName;
    }

    public Object getColumnValue() {
        return columnValue;
    }

    public FieldStrategy getInsertStrategy() {
        return insertStrategy;
    }

    public FieldStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    public static class JSONPropertyMapperBuilder {
        private String propertyExpression;
        private String propertyName;
        private Object propertyValue;
        private TransformStrategy transformStrategy = new NoneTransformStrategy();
        private Boolean primaryKey;
        private IdType idType;
        private String columnName;
        //        private Object columnValue;
        private FieldStrategy insertStrategy;
        private FieldStrategy updateStrategy;

        public JSONPropertyMapperBuilder() {
        }

        public JSONPropertyMapperBuilder(String propertyExpression, String columnName) {
            this(propertyExpression, false, null, columnName, new NoneTransformStrategy());
        }

        public JSONPropertyMapperBuilder(String propertyExpression, String columnName, TransformStrategy transformStrategy) {
            this(propertyExpression, false, null, columnName, transformStrategy, null);
        }

        public JSONPropertyMapperBuilder(String propertyExpression, String columnName, TransformStrategy transformStrategy, FieldStrategy insertStrategy) {
            this(propertyExpression, false, null, columnName, transformStrategy, insertStrategy);
        }

        public JSONPropertyMapperBuilder(String propertyExpression, String columnName, TransformStrategy transformStrategy, FieldStrategy insertStrategy, FieldStrategy updateStrategy) {
            this(propertyExpression, false, null, columnName, transformStrategy, insertStrategy, updateStrategy);
        }


        public JSONPropertyMapperBuilder(String propertyExpression, Boolean primaryKey, IdType idType, String columnName) {
            this(propertyExpression, primaryKey, idType, columnName, new NoneTransformStrategy());
        }

        public JSONPropertyMapperBuilder(String propertyExpression, Boolean primaryKey, IdType idType, String columnName, TransformStrategy transformStrategy) {
            this(propertyExpression, primaryKey, idType, columnName, transformStrategy, null);
        }

        public JSONPropertyMapperBuilder(String propertyExpression, Boolean primaryKey, IdType idType, String columnName, TransformStrategy transformStrategy, FieldStrategy insertStrategy) {
            this(propertyExpression, primaryKey, idType, columnName, transformStrategy, insertStrategy, null);
        }

        public JSONPropertyMapperBuilder(String propertyExpression, Boolean primaryKey, IdType idType, String columnName, TransformStrategy transformStrategy, FieldStrategy insertStrategy, FieldStrategy updateStrategy) {
            Assert.notBlank(columnName, "Invalid columnName");
            this.propertyExpression = propertyExpression;
            this.primaryKey = primaryKey;
            this.idType = idType;
            this.columnName = columnName;
            this.transformStrategy = transformStrategy;
            this.insertStrategy = insertStrategy;
            this.updateStrategy = updateStrategy;
            if (StringUtils.isNotBlank(propertyExpression)) {
                this.propertyName = propertyExpression.substring(propertyExpression.lastIndexOf(".") + 1)
                        .replace("[", "")
                        .replace("]", "")
                        .replace("*", "");
            } else {//指定propertyValue的情况，可以不声明propertyName，系统自动取columnName
                this.propertyName = this.columnName;
            }
        }

        public JSONPropertyMapperBuilder propertyExpression(String propertyExpression) {
            this.propertyExpression = propertyExpression;
            return this;
        }

        public JSONPropertyMapperBuilder propertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        public JSONPropertyMapperBuilder propertyValue(Object propertyValue) {
            this.propertyValue = propertyValue;
            return this;
        }

        public JSONPropertyMapperBuilder transformStrategy(TransformStrategy transformStrategy) {
            this.transformStrategy = transformStrategy;
            return this;
        }

        public JSONPropertyMapperBuilder primaryKey(Boolean primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        public JSONPropertyMapperBuilder idType(IdType idType) {
            this.idType = idType;
            return this;
        }

        public JSONPropertyMapperBuilder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

//        public JSONPropertyMapperBuilder columnValue(Object columnValue) {
//            this.columnValue = columnValue;
//            return this;
//        }

        public JSONPropertyMapperBuilder insertStrategy(FieldStrategy insertStrategy) {
            this.insertStrategy = insertStrategy;
            return this;
        }

        public JSONPropertyMapperBuilder updateStrategy(FieldStrategy updateStrategy) {
            this.updateStrategy = updateStrategy;
            return this;
        }

        public JSONPropertyMapperBuilder clone(JSONPropertyMapper mapper) {
            this.propertyExpression = mapper.propertyExpression;
            this.primaryKey = mapper.primaryKey;
            this.idType = mapper.idType;
            this.columnName = mapper.columnName;
            this.transformStrategy = mapper.transformStrategy;
            this.insertStrategy = mapper.insertStrategy;
            this.updateStrategy = mapper.updateStrategy;
            this.propertyName = mapper.propertyName;
            this.propertyValue = mapper.propertyValue;
            return this;
        }

        public Object getPropertyValue() {
            return propertyValue;
        }

        public void setPropertyValue(Object propertyValue) {
            this.propertyValue = propertyValue;
        }

        public JSONPropertyMapper build() {
            if (this.columnName == null) {
                throw new RuntimeException("invalid args:columnName could not be null");
            }
            if (this.propertyValue == null && this.propertyExpression == null) {
                throw new RuntimeException("invalid args:propertyExpression could not be null when propertyValue is null");
            }
            return new JSONPropertyMapper(this);
        }
    }
}
