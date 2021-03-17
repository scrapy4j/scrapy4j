package com.victory.scrapy4j.core.support.mybatis.core.metadata;

import com.victory.scrapy4j.core.support.mybatis.annotation.FieldFill;
import com.victory.scrapy4j.core.support.mybatis.annotation.FieldStrategy;
import com.victory.scrapy4j.core.support.mybatis.annotation.SqlCondition;
import com.victory.scrapy4j.core.support.mybatis.toolkit.Constants;
import com.victory.scrapy4j.core.support.mybatis.toolkit.StringUtils;
import com.victory.scrapy4j.core.support.mybatis.toolkit.SqlScriptUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/*
copied from mybatis-plus
*/
public class TableFieldInfo implements Constants {

    /**
     * 字段名
     */
    private String column;
    /**
     * 属性名
     */
    private String property;
    /**
     * 属性表达式#{property}, 可以指定jdbcType, typeHandler等
     */
    private String el;

    private boolean isCharSequence;
    /**
     * 字段验证策略之 insert
     *
     *
     * @since added v_3.1.2 @2019-5-7
     */
    private FieldStrategy insertStrategy;
    /**
     * 字段验证策略之 update
     *
     *
     * @since added v_3.1.2 @2019-5-7
     */
    private FieldStrategy updateStrategy;
    /**
     * 是否进行 select 查询
     * <p>大字段可设置为 false 不加入 select 查询范围</p>
     */
    private boolean select = true;
    /**
     * 字段 update set 部分注入
     */
    private String update;
    /**
     * where 字段比较条件
     */
    private String condition = SqlCondition.EQUAL;
    /**
     * 字段填充策略
     */
    private FieldFill fieldFill = FieldFill.DEFAULT;
    /**
     * 表字段是否启用了插入填充
     *
     * @since 3.3.0
     */
    private boolean withInsertFill;
    /**
     * 表字段是否启用了更新填充
     *
     * @since 3.3.0
     */
    private boolean withUpdateFill;

    private String sqlSelect;

    /**
     * JDBC类型
     *
     * @since 3.1.2
     */
    private JdbcType jdbcType;
    /**
     * 类型处理器
     *
     * @since 3.1.2
     */
    private Class<? extends TypeHandler<?>> typeHandler;

    public TableFieldInfo() {

    }

    public TableFieldInfo(String property) {
        this.property = property;
    }

    public TableFieldInfo(String property, String column) {
        this.property = property;
        this.column = column;
    }


    public TableFieldInfo(String property, String column, FieldStrategy insertStrategy, FieldStrategy updateStrategy) {
        this.property = property;
        this.column = column;
        this.insertStrategy = insertStrategy;
        this.updateStrategy = updateStrategy;
        this.sqlSelect = column;
    }

    /**
     * 不存在 TableField 注解时, 使用的构造函数
     */
    public TableFieldInfo(TableInfo tableInfo, String property, String column, FieldStrategy insertStrategy, FieldStrategy updateStrategy) {
        this.property = property;
//        this.isCharSequence = StringUtils.isCharSequence(this.propertyType);
//        this.isCharSequence = StringUtils.isCharSequence(String.class);
        this.el = this.property;
        this.insertStrategy = insertStrategy;
        this.updateStrategy = updateStrategy;

//        this.whereStrategy = dbConfig.getSelectStrategy();

        if (tableInfo.isUnderCamel()) {
            /* 开启字段下划线申明 */
            column = StringUtils.camelToUnderline(column);
        }
        if (tableInfo.isCapitalMode()) {
            /* 开启字段全大写申明 */
            column = column.toUpperCase();
        }

//        String columnFormat = GlobalConfig.DbConfig.getColumnFormat();
//        if (StringUtils.isNotBlank(columnFormat)) {
//            column = String.format(columnFormat, column);
//        }

        this.column = column;
        this.sqlSelect = column;
    }


    /**
     * 获取 insert 时候插入值 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * <li> 不生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getInsertSqlProperty(final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        return SqlScriptUtils.safeParam(newPrefix + el) + COMMA;
    }

    /**
     * 获取 insert 时候插入值 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * <li> 根据规则会生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getInsertSqlPropertyMaybeIf(final String prefix) {
        String sqlScript = getInsertSqlProperty(prefix);
        if (withInsertFill) {
            return sqlScript;
        }
        return convertIf(sqlScript, property, insertStrategy);
    }

    /**
     * 获取 insert 时候字段 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "字段" 部位</p>
     *
     * <li> 不生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getInsertSqlColumn() {
        return column + COMMA;
    }

    /**
     * 获取 insert 时候字段 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "字段" 部位</p>
     *
     * <li> 根据规则会生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getInsertSqlColumnMaybeIf() {
        final String sqlScript = getInsertSqlColumn();
        if (withInsertFill) {
            return sqlScript;
        }
        return convertIf(sqlScript, property, insertStrategy);
    }

    /**
     * 获取 set sql 片段
     *
     * @param prefix 前缀
     * @return sql 脚本片段
     */
    public String getSqlSet(final String prefix) {
        return getSqlSet(false, prefix);
    }

    /**
     * 获取 set sql 片段
     *
     * @param ignoreIf 忽略 IF 包裹
     * @param prefix   前缀
     * @return sql 脚本片段
     */
    public String getSqlSet(final boolean ignoreIf, final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        // 默认: column=
        String sqlSet = column + EQUALS;
        if (StringUtils.isNotBlank(update)) {
            sqlSet += String.format(update, column);
        } else {
            sqlSet += SqlScriptUtils.safeParam(newPrefix + el);
        }
        sqlSet += COMMA;
        if (ignoreIf) {
            return sqlSet;
        }
        if (withUpdateFill) {
            // 不进行 if 包裹
            return sqlSet;
        }
        return convertIf(sqlSet, convertIfProperty(prefix, property), updateStrategy);
    }

    private String convertIfProperty(String prefix, String property) {
        return StringUtils.isNotBlank(prefix) ? prefix.substring(0, prefix.length() - 1) + "['" + property + "']" : property;
    }


    /**
     * 转换成 if 标签的脚本片段
     *
     * @param sqlScript     sql 脚本片段
     * @param property      字段名
     * @param fieldStrategy 验证策略
     * @return if 脚本片段
     */
    private String convertIf(final String sqlScript, final String property, final FieldStrategy fieldStrategy) {
        if (fieldStrategy == FieldStrategy.NEVER) {
            return null;
        }
        if (fieldStrategy == FieldStrategy.IGNORED) {
            return sqlScript;
        }
        if (fieldStrategy == FieldStrategy.NOT_EMPTY && isCharSequence) {
            return SqlScriptUtils.convertIf(sqlScript, String.format("%s != null and %s != ''", property, property),
                    false);
        }
        return SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", property), false);
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public FieldStrategy getInsertStrategy() {
        return insertStrategy;
    }

    public void setInsertStrategy(FieldStrategy insertStrategy) {
        this.insertStrategy = insertStrategy;
    }

    public FieldStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(FieldStrategy updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    public FieldFill getFieldFill() {
        return fieldFill;
    }

    public void setFieldFill(FieldFill fieldFill) {
        this.fieldFill = fieldFill;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(JdbcType jdbcType) {
        this.jdbcType = jdbcType;
    }

    public boolean isWithInsertFill() {
        return withInsertFill;
    }

    public void setWithInsertFill(boolean withInsertFill) {
        this.withInsertFill = withInsertFill;
    }

    public boolean isWithUpdateFill() {
        return withUpdateFill;
    }

    public void setWithUpdateFill(boolean withUpdateFill) {
        this.withUpdateFill = withUpdateFill;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public String getSqlSelect() {
        return sqlSelect;
    }

    public void setSqlSelect(String sqlSelect) {
        this.sqlSelect = sqlSelect;
    }
}