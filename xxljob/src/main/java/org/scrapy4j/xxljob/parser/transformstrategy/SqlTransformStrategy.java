package org.scrapy4j.xxljob.parser.transformstrategy;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import org.scrapy4j.core.support.mybatis.method.AbstractMethod;
import org.scrapy4j.core.support.mybatis.toolkit.SqlUtils;
import org.scrapy4j.xxljob.parser.JSONPropertyMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlTransformStrategy implements TransformStrategy {

    private final static String TABLE_NAME = "anyTableName";

    private final static String ognlRegex = "#\\{([^\\}]*)\\}";

    private SqlSessionTemplate sqlSessionTemplate;

    private String sql;

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public Object exec(Object propertyValue, List<JSONPropertyMapper> currentRow, List<List<JSONPropertyMapper>> allRows) {
        if (propertyValue != null) {
            Map<String, String> param = this.generateParam(currentRow);
            Map<String, Object> data = sqlSessionTemplate.selectOne(AbstractMethod.getStatementName(TABLE_NAME, SqlMethod.RAW_SQL_SELECT_LIST.getMethod()), param);
            if (data != null && !data.isEmpty()) {
                return data.values().stream().findFirst().get();
            }
            return propertyValue;
        }
        return propertyValue;
    }

    private Map<String, String> generateParam(List<JSONPropertyMapper> currentRow) {
        String finalSql = sql;
        Map<String, String> param = new HashMap<>();
        Map<String, Object> record = this.curentRowRecord(currentRow);
        if (record != null && !record.isEmpty()) {
            Pattern pattern = Pattern.compile(ognlRegex);
            Matcher matcher = pattern.matcher(sql);
            while (matcher.find()) {
                String varKey = matcher.group(1);
                if (record.containsKey(varKey)) {
                    finalSql = StringUtils.replace(finalSql, matcher.group(), String.format("'%s'", MapUtils.getString(record, varKey)));
                }
            }
        }
        param.put("sql", SqlUtils.removeSQLInjectionAttackWords(finalSql));
        return param;
    }

    /**
     * @param currentRow
     * @return
     */
    private Map<String, Object> curentRowRecord(List<JSONPropertyMapper> currentRow) {
        Map<String, Object> record = new HashMap<>();
        if (currentRow != null && currentRow.size() > 0) {
            for (JSONPropertyMapper mapper : currentRow) {
                record.put(mapper.getColumnName(), mapper.getPropertyValue());
            }
        }
        return record;
    }
}
