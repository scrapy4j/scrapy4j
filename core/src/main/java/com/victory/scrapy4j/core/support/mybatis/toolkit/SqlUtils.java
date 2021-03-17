package com.victory.scrapy4j.core.support.mybatis.toolkit;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlUtils {
    public final static int DATABASETYPE_ORACLE = 0;// 数据库类型为oracle
    public final static int DATABASETYPE_SQLSERVER = 1; // 数据库类型为sqlserver
    public final static int DATABASETYPE_MYSQL = 2;// 数据库类型为mysql
    public final static int DATABASETYPE_DB2 = 3;// 数据库类型为db2

    private static String[] SQLInjectionAttackPatterns = {"\\binsert\\b", "\\bdelete\\b", "\\bdrop\\b", "\\bupdate\\b", "\\btruncate\\b", "\\bsleep\\b", ";", "--"};

    public static Object removeSQLInjectionAttackWords(Object value) {
        Object[] values = null;
        if (value instanceof String) {
            value = removeSQLInjectionAttackWords((String) value);
        } else if (value instanceof Object[]) {
            values = (Object[]) value;
        } else if (value instanceof List) {
            values = ((List<Object>) value).toArray();
        }
        if (values != null && values.length > 0) {
            for (int i = 0, l = values.length; i < l; i++) {
                if (values[i] != null && values[i] instanceof String) {
                    values[i] = removeSQLInjectionAttackWords(values[i].toString());
                }
            }
            value = values;
        }
        return value;
    }

    public static String removeSQLInjectionAttackWords(String value) {
        if (StringUtils.isNotBlank(value)) {
            for (String pattern : SQLInjectionAttackPatterns) {
                Matcher sqlMatcher = Pattern.compile(pattern).matcher(value);
                value = sqlMatcher.replaceAll("");
            }
        }
        return value;
    }

    /**
     * 拼接翻页的SQL语句
     *
     * @param sql         【SQL语句】
     * @param currentPage 【当前页】
     * @param pageSize    【每页记录数量】
     * @return
     */
    public static String makePageSQL(int dbType, String sql, int currentPage, int pageSize) {
        int mycurrentPage = currentPage;
        if (mycurrentPage < 1) {
            mycurrentPage = 1;
        }
        if (pageSize == 0) {
            return sql;
        }
        // 先按查询条件查询出从0到页未的记录.然后再取出从页开始到页未的记录
        StringBuilder sqlBuffer = new StringBuilder();
        if (dbType == DATABASETYPE_ORACLE) {
            if (currentPage <= 1) {
                sqlBuffer.append("select * from (").append(sql).append(")  where rownum<=").append(pageSize);
            } else {
                int startPos = (mycurrentPage - 1) * pageSize;
                int endPos = mycurrentPage * pageSize;
                sqlBuffer.append("select * from ( select row_.*, rownum rownum_ from ( ").append(sql).append(" ) row_ ").append(" where rownum<=").append(endPos).append(" ) where rownum_ >").append(startPos);
            }

        } else if (dbType == DATABASETYPE_SQLSERVER) {
            if (currentPage <= 1) {
                sqlBuffer.append("select * from (select row_number()over(").append(getOrderAfter(sql)).append(") rownum_,row_.* from(").append(getOrderBefore(sql)).append(") row_ ) row2_").append(" where rownum_ <= ").append(pageSize);
            } else {

                int startPos = (mycurrentPage - 1) * pageSize;
                int endPos = mycurrentPage * pageSize;
                sqlBuffer.append("select * from (select row_number()over(").append(getOrderAfter(sql)).append(") rownum_,row_.* from(").append(getOrderBefore(sql)).append(") row_ ) row2_").append(" where rownum_ <= ").append(endPos).append(" and rownum_ >").append(startPos);
            }

        } else if (dbType == DATABASETYPE_MYSQL) {
            int startPos = (mycurrentPage - 1) * pageSize;
            sqlBuffer.append(sql).append(" LIMIT ").append(startPos).append(",").append(pageSize);

        } else if (dbType == DATABASETYPE_DB2) {
            if (currentPage <= 1) {
                sqlBuffer.append("select * from (select row_number() over() AS rownum_,row_.* from (").append(sql).append(") row_) row2_ where rownum_<=").append(pageSize);
            } else {
                int startPos = (mycurrentPage - 1) * pageSize;
                int endPos = mycurrentPage * pageSize;
                sqlBuffer.append("select * from (select row_number()over(").append(getOrderAfter(sql)).append(") rownum_,row_.* from(").append(getOrderBefore(sql)).append(") row_ ) row2_").append(" where rownum_ <= ").append(endPos).append(" and rownum_ >").append(startPos);
            }
        }
        return sqlBuffer.toString();
    }

    /**
     * 截取SQL语句中最后的order by条件【包含order by】
     *
     * @param SQL
     * @return
     */
    public static String getOrderAfter(String SQL) {
        if (StringUtils.isEmpty(SQL)) {
            return "";
        }
        // 把SQL语句变成全小写
        String sql = SQL.toLowerCase();
        // 去掉条件中的''符号
        sql = sql.replaceAll("''", "  ");

        // 去掉条件中字符串类型的常量值
        sql = replaceAllDyh(sql);

        // 去掉带括号的子查询
        sql = replaceAllKh(sql);

        int e = sql.lastIndexOf(" order ");
        if (e != -1) {
            return SQL.substring(e, sql.length());
        } else {
            return "";
        }
    }

    /**
     * 截取SQL语句中order by以前的字符串内容【不包含order】
     *
     * @param SQL
     * @return
     */
    public static String getOrderBefore(String SQL) {
        if (StringUtils.isEmpty(SQL)) {
            return "";
        }
        // 把SQL语句变成全小写
        String sql = SQL.toLowerCase();
        // 去掉条件中的''符号
        sql = sql.replaceAll("''", "  ");

        // 去掉条件中字符串类型的常量值
        sql = replaceAllDyh(sql);

        // 去掉带括号的子查询
        sql = replaceAllKh(sql);

        int e = sql.lastIndexOf(" order ");
        if (e != -1) {
            return SQL.substring(0, e);
        } else {
            return SQL;
        }
    }

    /**
     * 把小括号()及括号中的内容替换成空格
     *
     * @param SQL
     * @return
     */
    private static String replaceAllKh(String SQL) {
        if (StringUtils.isEmpty(SQL)) {
            return "";
        }
        String mysql = SQL;
        int b = mysql.lastIndexOf('(');
        int e = mysql.indexOf(")");
        String temp = mysql;
        while (b > e) {
            temp = temp.substring(0, e) + " " + temp.substring(e + 1, temp.length());
            e = temp.indexOf(")");
        }
        if (b != -1) {
            temp = "";
            for (int i = b; i <= e; i++) {
                temp += " ";
            }
            mysql = mysql.substring(0, b) + temp + mysql.substring(e + 1, mysql.length());
        }
        if (mysql.lastIndexOf('(') != -1) {
            mysql = replaceAllKh(mysql);
        }
        return mysql;
    }

    /**
     * 把条件中的单引号''及引号中的内容替换成功空格
     *
     * @param sql
     * @return
     */
    private static String replaceAllDyh(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return "";
        }
        String mysql = sql;
        int b = mysql.indexOf("'");
        if (b != -1) {
            mysql = mysql.substring(0, b) + " " + mysql.substring(b + 1, mysql.length());
        }
        int e = mysql.indexOf("'");
        if (e != -1) {
            String temp = "";
            for (int i = b; i <= e; i++) {
                temp += " ";
            }
            mysql = mysql.substring(0, b) + temp + mysql.substring(e + 1, mysql.length());
        }
        if (mysql.indexOf("'") != -1) {
            mysql = replaceAllDyh(mysql);
        }
        return mysql;
    }
}
