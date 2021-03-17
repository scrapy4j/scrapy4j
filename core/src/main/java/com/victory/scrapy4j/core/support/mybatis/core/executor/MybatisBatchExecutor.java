package com.victory.scrapy4j.core.support.mybatis.core.executor;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchExecutorException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

public class MybatisBatchExecutor extends AbstractBaseExecutor {
    public static final int BATCH_UPDATE_RETURN_VALUE = -2147482646;
    private final List<Statement> statementList = new ArrayList();
    private final List<BatchResult> batchResultList = new ArrayList();
    private String currentSql;
    private MappedStatement currentStatement;

    public MybatisBatchExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    public int doUpdate(MappedStatement ms, Object parameterObject) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(this, ms, parameterObject, RowBounds.DEFAULT, (ResultHandler)null, (BoundSql)null);
        BoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();
        Statement stmt;
        if (sql.equals(this.currentSql) && ms.equals(this.currentStatement)) {
            int last = this.statementList.size() - 1;
            stmt = (Statement)this.statementList.get(last);
            this.applyTransactionTimeout(stmt);
            handler.parameterize(stmt);
            BatchResult batchResult = (BatchResult)this.batchResultList.get(last);
            batchResult.addParameterObject(parameterObject);
        } else {
            Connection connection = this.getConnection(ms.getStatementLog());
            stmt = handler.prepare(connection, this.transaction.getTimeout());
            if (stmt == null) {
                return 0;
            }

            handler.parameterize(stmt);
            this.currentSql = sql;
            this.currentStatement = ms;
            this.statementList.add(stmt);
            this.batchResultList.add(new BatchResult(ms, sql, parameterObject));
        }

        handler.batch(stmt);
        return -2147482646;
    }

    public <E> List<E> doQuery(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Statement stmt = null;

        List var10;
        try {
            this.flushStatements();
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(this.wrapper, ms, parameterObject, rowBounds, resultHandler, boundSql);
            Connection connection = this.getConnection(ms.getStatementLog());
            stmt = handler.prepare(connection, this.transaction.getTimeout());
            if (stmt == null) {
                var10 = Collections.emptyList();
                return var10;
            }

            handler.parameterize(stmt);
            var10 = handler.query(stmt, resultHandler);
        } finally {
            this.closeStatement(stmt);
        }

        return var10;
    }

    protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
        this.flushStatements();
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(this.wrapper, ms, parameter, rowBounds, (ResultHandler)null, boundSql);
        Connection connection = this.getConnection(ms.getStatementLog());
        Statement stmt = handler.prepare(connection, this.transaction.getTimeout());
        handler.parameterize(stmt);
        Cursor<E> cursor = handler.queryCursor(stmt);
        stmt.closeOnCompletion();
        return cursor;
    }

    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
        boolean var17 = false;

        Statement stmt;
        ArrayList var21;
        Iterator var22;
        label179: {
            List var3;
            try {
                var17 = true;
                ArrayList results = new ArrayList();
                if (!isRollback) {
                    int i = 0;

                    for(int n = this.statementList.size(); i < n; ++i) {
                        stmt = (Statement)this.statementList.get(i);
                        this.applyTransactionTimeout(stmt);
                        BatchResult batchResult = (BatchResult)this.batchResultList.get(i);

                        try {
                            batchResult.setUpdateCounts(stmt.executeBatch());
                            MappedStatement ms = batchResult.getMappedStatement();
                            List<Object> parameterObjects = batchResult.getParameterObjects();
                            KeyGenerator keyGenerator = ms.getKeyGenerator();
                            if (Jdbc3KeyGenerator.class.equals(keyGenerator.getClass())) {
                                Jdbc3KeyGenerator jdbc3KeyGenerator = (Jdbc3KeyGenerator)keyGenerator;
                                jdbc3KeyGenerator.processBatch(ms, stmt, parameterObjects);
                            } else if (!NoKeyGenerator.class.equals(keyGenerator.getClass())) {
                                Iterator var10 = parameterObjects.iterator();

                                while(var10.hasNext()) {
                                    Object parameter = var10.next();
                                    keyGenerator.processAfter(this, ms, stmt, parameter);
                                }
                            }

                            this.closeStatement(stmt);
                        } catch (BatchUpdateException var18) {
                            StringBuilder message = new StringBuilder();
                            message.append(batchResult.getMappedStatement().getId()).append(" (batch index #").append(i + 1).append(")").append(" failed.");
                            if (i > 0) {
                                message.append(" ").append(i).append(" prior sub executor(s) completed successfully, but will be rolled back.");
                            }

                            throw new BatchExecutorException(message.toString(), var18, results, batchResult);
                        }

                        results.add(batchResult);
                    }

                    var21 = results;
                    var17 = false;
                    break label179;
                }

                var3 = Collections.emptyList();
                var17 = false;
            } finally {
                if (var17) {
                    Iterator var13 = this.statementList.iterator();

                    while(var13.hasNext()) {
                        stmt = (Statement)var13.next();
                        this.closeStatement(stmt);
                    }

                    this.currentSql = null;
                    this.statementList.clear();
                    this.batchResultList.clear();
                }
            }

            var22 = this.statementList.iterator();

            while(var22.hasNext()) {
                stmt = (Statement)var22.next();
                this.closeStatement(stmt);
            }

            this.currentSql = null;
            this.statementList.clear();
            this.batchResultList.clear();
            return var3;
        }

        var22 = this.statementList.iterator();

        while(var22.hasNext()) {
            stmt = (Statement)var22.next();
            this.closeStatement(stmt);
        }

        this.currentSql = null;
        this.statementList.clear();
        this.batchResultList.clear();
        return var21;
    }
}
