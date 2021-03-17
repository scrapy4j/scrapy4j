package com.victory.scrapy4j.core.support.mybatis.core.parser;

import com.victory.scrapy4j.core.support.mybatis.toolkit.SqlParserUtils;
import com.victory.scrapy4j.core.support.mybatis.toolkit.CollectionUtils;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.MetaObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class JsqlParserCountOptimize implements ISqlParser {
    private static final List<SelectItem> COUNT_SELECT_ITEM = countSelectItem();
    private final Log logger = LogFactory.getLog(JsqlParserCountOptimize.class);
    private boolean optimizeJoin = false;

    private static List<SelectItem> countSelectItem() {
        Function function = new Function();
        function.setName("COUNT");
        List<Expression> expressions = new ArrayList();
        LongValue longValue = new LongValue(1L);
        ExpressionList expressionList = new ExpressionList();
        expressions.add(longValue);
        expressionList.setExpressions(expressions);
        function.setParameters(expressionList);
        List<SelectItem> selectItems = new ArrayList();
        SelectExpressionItem selectExpressionItem = new SelectExpressionItem(function);
        selectItems.add(selectExpressionItem);
        return selectItems;
    }

    public SqlInfo parser(MetaObject metaObject, String sql) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("JsqlParserCountOptimize sql=" + sql);
        }

        SqlInfo sqlInfo = SqlInfo.newInstance();

        try {
            Select selectStatement = (Select)CCJSqlParserUtil.parse(sql);
            PlainSelect plainSelect = (PlainSelect)selectStatement.getSelectBody();
            Distinct distinct = plainSelect.getDistinct();
            GroupByElement groupBy = plainSelect.getGroupBy();
            List<OrderByElement> orderBy = plainSelect.getOrderByElements();
            if (null == groupBy && CollectionUtils.isNotEmpty(orderBy)) {
                plainSelect.setOrderByElements((List)null);
                sqlInfo.setOrderBy(false);
            }

            Iterator var9 = plainSelect.getSelectItems().iterator();

            while(var9.hasNext()) {
                SelectItem item = (SelectItem)var9.next();
                if (item.toString().contains("?")) {
                    return sqlInfo.setSql(SqlParserUtils.getOriginalCountSql(selectStatement.toString()));
                }
            }

            if (distinct == null && null == groupBy) {
                List<Join> joins = plainSelect.getJoins();
                if (this.optimizeJoin && CollectionUtils.isNotEmpty(joins)) {
                    boolean canRemoveJoin = true;
                    String whereS = Optional.ofNullable(plainSelect.getWhere()).map(Object::toString).orElse("");
                    Iterator var12 = joins.iterator();

                    label54: {
                        String str;
                        String onExpressionS;
                        do {
                            if (!var12.hasNext()) {
                                break label54;
                            }

                            Join join = (Join)var12.next();
                            if (!join.isLeft()) {
                                canRemoveJoin = false;
                                break label54;
                            }

                            Table table = (Table)join.getRightItem();
                            str = (String)Optional.ofNullable(table.getAlias()).map(Alias::getName).orElse(table.getName()) + ".";
                            onExpressionS = join.getOnExpression().toString();
                        } while(!onExpressionS.contains("?") && !whereS.contains(str));

                        canRemoveJoin = false;
                    }

                    if (canRemoveJoin) {
                        plainSelect.setJoins((List)null);
                    }
                }

                plainSelect.setSelectItems(COUNT_SELECT_ITEM);
                return sqlInfo.setSql(selectStatement.toString());
            } else {
                return sqlInfo.setSql(SqlParserUtils.getOriginalCountSql(selectStatement.toString()));
            }
        } catch (Throwable var17) {
            return sqlInfo.setSql(SqlParserUtils.getOriginalCountSql(sql));
        }
    }

    public Log getLogger() {
        return this.logger;
    }

    public boolean isOptimizeJoin() {
        return this.optimizeJoin;
    }

    public void setOptimizeJoin(final boolean optimizeJoin) {
        this.optimizeJoin = optimizeJoin;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof JsqlParserCountOptimize)) {
            return false;
        } else {
            JsqlParserCountOptimize other = (JsqlParserCountOptimize)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$logger = this.getLogger();
                Object other$logger = other.getLogger();
                if (this$logger == null) {
                    if (other$logger == null) {
                        return this.isOptimizeJoin() == other.isOptimizeJoin();
                    }
                } else if (this$logger.equals(other$logger)) {
                    return this.isOptimizeJoin() == other.isOptimizeJoin();
                }

                return false;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof JsqlParserCountOptimize;
    }

    public int hashCode() {
        int result = 1;
        Object $logger = this.getLogger();
        result = result * 59 + ($logger == null ? 43 : $logger.hashCode());
        result = result * 59 + (this.isOptimizeJoin() ? 79 : 97);
        return result;
    }

    public String toString() {
        return "JsqlParserCountOptimize(logger=" + this.getLogger() + ", optimizeJoin=" + this.isOptimizeJoin() + ")";
    }

    public JsqlParserCountOptimize() {
    }

    public JsqlParserCountOptimize(final boolean optimizeJoin) {
        this.optimizeJoin = optimizeJoin;
    }
}