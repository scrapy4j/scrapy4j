package com.victory.scrapy4j.core.support.mybatis.core.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
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

public class MybatisSimpleExecutor extends AbstractBaseExecutor {
    public MybatisSimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;

        int var6;
        try {
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, (ResultHandler)null, (BoundSql)null);
            stmt = this.prepareStatement(handler, ms.getStatementLog(), false);
            var6 = stmt == null ? 0 : handler.update(stmt);
        } finally {
            this.closeStatement(stmt);
        }

        return var6;
    }

    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Statement stmt = null;

        List var9;
        try {
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(this.wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
            stmt = this.prepareStatement(handler, ms.getStatementLog(), false);
            var9 = stmt == null ? Collections.emptyList() : handler.query(stmt, resultHandler);
        } finally {
            this.closeStatement(stmt);
        }

        return var9;
    }

    protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(this.wrapper, ms, parameter, rowBounds, (ResultHandler)null, boundSql);
        Statement stmt = this.prepareStatement(handler, ms.getStatementLog(), true);

        assert stmt != null;

        Cursor<E> cursor = handler.queryCursor(stmt);
        stmt.closeOnCompletion();
        return cursor;
    }

    public List<BatchResult> doFlushStatements(boolean isRollback) {
        return Collections.emptyList();
    }

    private Statement prepareStatement(StatementHandler handler, Log statementLog, boolean isCursor) throws SQLException {
        Connection connection = this.getConnection(statementLog);
        Statement stmt = handler.prepare(connection, this.transaction.getTimeout());
        if (stmt == null && !isCursor) {
            return null;
        } else {
            handler.parameterize(stmt);
            return stmt;
        }
    }
}
