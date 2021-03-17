package com.victory.scrapy4j.core.support.mybatis.core.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

public class MybatisReuseExecutor extends AbstractBaseExecutor {
    private final Map<String, Statement> statementMap = new HashMap();

    public MybatisReuseExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, (ResultHandler)null, (BoundSql)null);
        Statement stmt = this.prepareStatement(handler, ms.getStatementLog(), false);
        return stmt == null ? 0 : handler.update(stmt);
    }

    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(this.wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        Statement stmt = this.prepareStatement(handler, ms.getStatementLog(), false);
        return stmt == null ? Collections.emptyList() : handler.query(stmt, resultHandler);
    }

    protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(this.wrapper, ms, parameter, rowBounds, (ResultHandler)null, boundSql);
        Statement stmt = this.prepareStatement(handler, ms.getStatementLog(), true);
        return handler.queryCursor(stmt);
    }

    public List<BatchResult> doFlushStatements(boolean isRollback) {
        Iterator var2 = this.statementMap.values().iterator();

        while(var2.hasNext()) {
            Statement stmt = (Statement)var2.next();
            this.closeStatement(stmt);
        }

        this.statementMap.clear();
        return Collections.emptyList();
    }

    private Statement prepareStatement(StatementHandler handler, Log statementLog, boolean isCursor) throws SQLException {
        BoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();
        Statement stmt;
        if (this.hasStatementFor(sql)) {
            stmt = this.getStatement(sql);
            this.applyTransactionTimeout(stmt);
        } else {
            Connection connection = this.getConnection(statementLog);
            stmt = handler.prepare(connection, this.transaction.getTimeout());
            if (stmt == null && !isCursor) {
                return null;
            }

            this.putStatement(sql, stmt);
        }

        handler.parameterize(stmt);
        return stmt;
    }

    private boolean hasStatementFor(String sql) {
        try {
            return this.statementMap.containsKey(sql) && !((Statement)this.statementMap.get(sql)).getConnection().isClosed();
        } catch (SQLException var3) {
            return false;
        }
    }

    private Statement getStatement(String s) {
        return (Statement)this.statementMap.get(s);
    }

    private void putStatement(String sql, Statement stmt) {
        this.statementMap.put(sql, stmt);
    }
}
