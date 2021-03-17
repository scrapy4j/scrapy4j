package com.victory.scrapy4j.core.support.mybatis.core.parser;

public class SqlInfo {
    private String sql;
    private boolean orderBy = true;

    public static SqlInfo newInstance() {
        return new SqlInfo();
    }

    public SqlInfo() {
    }

    public String getSql() {
        return this.sql;
    }

    public boolean isOrderBy() {
        return this.orderBy;
    }

    public SqlInfo setSql(final String sql) {
        this.sql = sql;
        return this;
    }

    public SqlInfo setOrderBy(final boolean orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof SqlInfo)) {
            return false;
        } else {
            SqlInfo other = (SqlInfo)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$sql = this.getSql();
                Object other$sql = other.getSql();
                if (this$sql == null) {
                    if (other$sql == null) {
                        return this.isOrderBy() == other.isOrderBy();
                    }
                } else if (this$sql.equals(other$sql)) {
                    return this.isOrderBy() == other.isOrderBy();
                }

                return false;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SqlInfo;
    }

    public int hashCode() {
        int result = 1;
        Object $sql = this.getSql();
        result = result * 59 + ($sql == null ? 43 : $sql.hashCode());
        result = result * 59 + (this.isOrderBy() ? 79 : 97);
        return result;
    }

    public String toString() {
        return "SqlInfo(sql=" + this.getSql() + ", orderBy=" + this.isOrderBy() + ")";
    }
}
