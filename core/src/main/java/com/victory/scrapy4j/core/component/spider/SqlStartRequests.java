package com.victory.scrapy4j.core.component.spider;

import com.victory.scrapy4j.core.component.pojo.Request;
import com.victory.scrapy4j.core.component.pojo.StartSql;
import com.victory.scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import com.victory.scrapy4j.core.support.mybatis.core.metadata.TableInfo;
import com.victory.scrapy4j.core.support.mybatis.core.parser.JsqlParserCountOptimize;
import com.victory.scrapy4j.core.support.mybatis.core.parser.SqlInfo;
import com.victory.scrapy4j.core.support.mybatis.toolkit.SqlParserUtils;
import com.victory.scrapy4j.core.support.mybatis.method.AbstractMethod;
import com.victory.scrapy4j.core.support.mybatis.method.RawSqlSelectList;
import com.victory.scrapy4j.core.support.mybatis.toolkit.DynamicSqlInjector;
import com.victory.scrapy4j.core.support.mybatis.toolkit.SqlUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class SqlStartRequests extends StartRequests {
    private final String TABLE_NAME = "anyTableName";
    private final StartSql startSql;
    private final SqlSessionTemplate sqlSessionTemplate;
    private boolean init = false;
    private List<Map<String, Object>> dataList;
    private long dataTotal;
    private AtomicInteger currentRowNum = new AtomicInteger(1);
    private AtomicInteger currentPageNum = new AtomicInteger(1);
    /**
     * SQL返回数据加工函数
     */
    private Function<Map<String, Object>, Map<String, Object>> sqlVarResolver;

    private SqlStartRequests(SqlStartRequestsBuilder builder) {
        super(builder);
        this.startSql = builder.startSql;
        this.sqlSessionTemplate = builder.sqlSessionTemplate;
        new DynamicSqlInjector(new RawSqlSelectList()).inspectInject(sqlSessionTemplate.getConfiguration(), new TableInfo(TABLE_NAME));
    }

    public static SqlStartRequestsBuilder builder(String url, StartSql startSql, SqlSessionTemplate sqlSessionTemplate, feign.Request.HttpMethod httpMethod) {
        return new SqlStartRequestsBuilder(url, startSql, sqlSessionTemplate, httpMethod);
    }

    public static SqlStartRequestsBuilder builder(String url, StartSql startSql, SqlSessionTemplate sqlSessionTemplate) {
        return new SqlStartRequestsBuilder(url, startSql, sqlSessionTemplate, feign.Request.HttpMethod.GET);
    }

    @Override
    public boolean hasNext() {
        if (!init) {
            initData();
            init = true;
        }
        return !dataList.isEmpty();
    }

    /**
     * 数据初始化
     */
    private void initData() {
        Map map = new HashMap<String, String>();

        if (startSql.getSqlParameters() != null) {
            map.putAll(startSql.getSqlParameters());
        }
        if (startSql.getPageSize() != null) {
            initCount();
            String sql = SqlUtils.makePageSQL(SqlUtils.DATABASETYPE_MYSQL, startSql.getSql(), currentPageNum.get(), startSql.getPageSize());
            map.put("sql", SqlUtils.removeSQLInjectionAttackWords(sql));
        } else {
            map.put("sql", SqlUtils.removeSQLInjectionAttackWords(startSql.getSql()));
        }
        this.dataList = sqlSessionTemplate.selectList(AbstractMethod.getStatementName(TABLE_NAME, SqlMethod.RAW_SQL_SELECT_LIST.getMethod()), map);
        this.currentRowNum.set(this.dataList.size());
    }

    /**
     * 统计
     */
    private void initCount() {
        Map map = new HashMap<String, String>();
        SqlInfo sqlInfo = SqlParserUtils.getOptimizeCountSql(true, new JsqlParserCountOptimize(true), startSql.getSql());
        map.put("sql", SqlUtils.removeSQLInjectionAttackWords(sqlInfo.getSql()));
        Map<String, Long> selResult = sqlSessionTemplate.selectOne(AbstractMethod.getStatementName(TABLE_NAME, SqlMethod.RAW_SQL_SELECT_LIST.getMethod()), map);
        this.dataTotal = selResult.get("COUNT(1)");
    }

    @Override
    public Object next() {
        Map map = dataList.get(0);
        dataList.remove(map);
        if (sqlVarResolver != null) {
            map = sqlVarResolver.apply(map);
        }
        if (dataList.isEmpty() && startSql.getPageSize() != null && currentRowNum.get() < dataTotal) {
            Map sqlMap = new HashMap<String, String>();
            String sql = SqlUtils.makePageSQL(SqlUtils.DATABASETYPE_MYSQL, startSql.getSql(), currentPageNum.incrementAndGet(), startSql.getPageSize());
            sqlMap.put("sql", SqlUtils.removeSQLInjectionAttackWords(sql));

            if (startSql.getSqlParameters() != null) {
                sqlMap.putAll(startSql.getSqlParameters());
            }

            this.dataList = sqlSessionTemplate.selectList(AbstractMethod.getStatementName(TABLE_NAME, SqlMethod.RAW_SQL_SELECT_LIST.getMethod()), sqlMap);
            currentRowNum.set(currentRowNum.get() + this.dataList.size());
        }

        RequestBuilder requestBuilder = Request.builder(this.getUrl(), this.getHttpMethod());
        if (this.getHeaders() != null) {
            requestBuilder.headers(this.getHeaders());
        }
        if (this.getQueries() != null) {
            requestBuilder.queries(this.getQueries());
        }
        if (this.getRequestBody() != null) {
            requestBuilder.requestBody(this.getRequestBody());
        }
        if (this.getParser() != null) {
            requestBuilder.parser(this.getParser());
        }
        requestBuilder.variables(map);
        if (this.getVariables() != null) {
            requestBuilder.addVariables(this.getVariables());
        }
        Request request = requestBuilder.build();
        request.setSpider(this.getSpider());
        return request;
    }

    public void addRequest(Request request) {
        throw new RuntimeException("do not support this method");
    }

    public void addRequests(List<Request> requests) {
        throw new RuntimeException("do not support this method");
    }

    public void setSqlVarResolver(Function<Map<String, Object>, Map<String, Object>> sqlVarResolver) {
        this.sqlVarResolver = sqlVarResolver;
    }

    public static class SqlStartRequestsBuilder extends RequestBuilder<SqlStartRequestsBuilder> {
        private final StartSql startSql;
        private final SqlSessionTemplate sqlSessionTemplate;

        public SqlStartRequestsBuilder(String url, StartSql startSql, SqlSessionTemplate sqlSessionTemplate, feign.Request.HttpMethod httpMethod) {
            super(url, httpMethod);
            this.startSql = startSql;
            this.sqlSessionTemplate = sqlSessionTemplate;
        }

        public SqlStartRequestsBuilder(String url, StartSql startSql, SqlSessionFactory sqlSessionFactory, feign.Request.HttpMethod httpMethod) {
            this(url, startSql, new SqlSessionTemplate(sqlSessionFactory), httpMethod);
        }

        public SqlStartRequests build() {
            return new SqlStartRequests(this);
        }
    }
}
