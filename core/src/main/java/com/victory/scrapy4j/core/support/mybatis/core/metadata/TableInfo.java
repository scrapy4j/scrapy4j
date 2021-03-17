package com.victory.scrapy4j.core.support.mybatis.core.metadata;

import cn.hutool.core.lang.Assert;
import com.victory.scrapy4j.core.support.mybatis.annotation.IdType;
import com.victory.scrapy4j.core.support.mybatis.annotation.KeySequence;
import com.victory.scrapy4j.core.support.mybatis.toolkit.Constants;
import com.victory.scrapy4j.core.support.mybatis.toolkit.StringUtils;
import com.victory.scrapy4j.core.support.mybatis.toolkit.SqlScriptUtils;
import org.apache.ibatis.session.Configuration;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

/*
copied from mybatis-plus
*/
public class TableInfo implements Constants {

    /**
     * 表主键ID 类型
     */
    private IdType idType = IdType.NONE;
    /**
     * 表名称
     */
    private String tableName;

    /**
     * 主键是否有存在字段名与属性名关联
     * <p>true: 表示要进行 as</p>
     */
    private boolean keyRelated;
    /**
     * 表主键ID 字段名
     */
    private String keyColumn;
    /**
     * 表主键ID 属性名
     */
    private String keyProperty;
    /**
     * 表主键ID 属性类型
     */
    private Class<?> keyType;
    /**
     * 表主键ID Sequence
     */
    private KeySequence keySequence;
    /**
     * 表字段信息列表
     */
    protected List<TableFieldInfo> fieldList;
    /**
     * 命名空间 (对应的 mapper 接口的全类名)
     */
    private String currentNamespace;
    /**
     * Configuration 标记 (Configuration内存地址值)
     */
    private Configuration configuration;

    private String allSqlSelect;
    private String sqlSelect;
    /**
     * 是否开启下划线转驼峰
     */
    private boolean underCamel = false;
    private boolean capitalMode = false;

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

    public TableInfo() {
    }

    public TableInfo(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 获得注入的 SQL Statement
     *
     * @param sqlMethod MybatisPlus 支持 SQL 方法
     * @return SQL Statement
     */
    public String getSqlStatement(String sqlMethod) {
        return currentNamespace + DOT + sqlMethod;
    }

    /**
     * 设置 Configuration
     */
    void setConfiguration(Configuration configuration) {
        Assert.notNull(configuration, "Error: You need Initialize MybatisConfiguration !");
        this.configuration = configuration;
        this.underCamel = configuration.isMapUnderscoreToCamelCase();
    }

    public String getAllSqlSelect() {
        if (this.allSqlSelect != null) {
            return this.allSqlSelect;
        } else {
            this.allSqlSelect = this.chooseSelect(TableFieldInfo::isSelect);
            return this.allSqlSelect;
        }
    }
    public String chooseSelect(Predicate<TableFieldInfo> predicate) {
        String sqlSelect = getKeySqlSelect();
        String fieldsSqlSelect = fieldList.stream().filter(predicate)
                .map(TableFieldInfo::getSqlSelect).collect(joining(COMMA));
        if (StringUtils.isNotBlank(sqlSelect) && StringUtils.isNotBlank(fieldsSqlSelect)) {
            return sqlSelect + COMMA + fieldsSqlSelect;
        } else if (StringUtils.isNotBlank(fieldsSqlSelect)) {
            return fieldsSqlSelect;
        }
        return sqlSelect;
    }
    public boolean havePK() {
        return StringUtils.isNotBlank(keyColumn);
    }

    public String getKeySqlSelect() {
        if (sqlSelect != null) {
            return sqlSelect;
        }
        if (havePK()) {
            sqlSelect = keyColumn;
        } else {
            sqlSelect = EMPTY;
        }
        return sqlSelect;
    }

    /**
     * 获取 insert 时候主键 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * @return sql 脚本片段
     */
    public String getKeyInsertSqlProperty(final String prefix, final boolean newLine) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        if (StringUtils.isNotBlank(keyProperty)) {
            if (idType == IdType.AUTO) {
                return EMPTY;
            }
            return SqlScriptUtils.safeParam(newPrefix + keyProperty) + COMMA + (newLine ? NEWLINE : EMPTY);
        }
        return EMPTY;
    }

    /**
     * 获取 insert 时候主键 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "字段" 部位</p>
     *
     * @return sql 脚本片段
     */
    public String getKeyInsertSqlColumn(final boolean newLine) {
        if (StringUtils.isBlank(keyColumn)) {
            keyColumn=keyProperty;
        }
        if (StringUtils.isNotBlank(keyColumn)) {
            if (idType == IdType.AUTO) {
                return EMPTY;
            }
            return keyColumn + COMMA + (newLine ? NEWLINE : EMPTY);
        }
        return EMPTY;
    }

    /**
     * 获取所有 insert 时候插入值 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "值" 部位</p>
     *
     * <li> 自动选部位,根据规则会生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getAllInsertSqlPropertyMaybeIf(final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        return getKeyInsertSqlProperty(newPrefix, true) + fieldList.stream()
                .map(i -> i.getInsertSqlPropertyMaybeIf(newPrefix)).filter(Objects::nonNull).collect(joining(NEWLINE));
    }

    /**
     * 获取 insert 时候字段 sql 脚本片段
     * <p>insert into table (字段) values (值)</p>
     * <p>位于 "字段" 部位</p>
     *
     * <li> 自动选部位,根据规则会生成 if 标签 </li>
     *
     * @return sql 脚本片段
     */
    public String getAllInsertSqlColumnMaybeIf() {
        return getKeyInsertSqlColumn(true) + fieldList.stream().map(TableFieldInfo::getInsertSqlColumnMaybeIf)
                .filter(Objects::nonNull).collect(joining(NEWLINE));
    }

    /**
     * 获取所有的 sql set 片段
     *
     * @param ignoreLogicDelFiled 是否过滤掉逻辑删除字段
     * @param prefix              前缀
     * @return sql 脚本片段
     */
    public String getAllSqlSet(boolean ignoreLogicDelFiled, final String prefix) {
        final String newPrefix = prefix == null ? EMPTY : prefix;
        return fieldList.stream()
                .map(i -> i.getSqlSet(newPrefix)).filter(Objects::nonNull).collect(joining(NEWLINE));
    }

    void setFieldList(List<TableFieldInfo> fieldList) {
        this.fieldList = fieldList;
        fieldList.forEach(i -> {
            if (i.isWithInsertFill()) {
                this.withInsertFill = true;
            }
            if (i.isWithUpdateFill()) {
                this.withUpdateFill = true;
            }
        });
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getKeyProperty() {
        return keyProperty;
    }

    public void setKeyProperty(String keyProperty) {
        this.keyProperty = keyProperty;
    }

    public boolean isUnderCamel() {
        return underCamel;
    }

    public void setUnderCamel(boolean underCamel) {
        this.underCamel = underCamel;
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

    public boolean isCapitalMode() {
        return capitalMode;
    }

    public void setCapitalMode(boolean capitalMode) {
        this.capitalMode = capitalMode;
    }

    public IdType getIdType() {
        return idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }
}
