package com.victory.scrapy4j.core.support.mybatis.core.metadata;

import java.util.ArrayList;
import java.util.List;

public final class PageList<T> extends ArrayList<T> {
    private List<T> records;
    private long total;

    public List<T> getRecords() {
        return this.records;
    }

    public long getTotal() {
        return this.total;
    }

    public void setRecords(final List<T> records) {
        this.records = records;
    }

    public void setTotal(final long total) {
        this.total = total;
    }

    public String toString() {
        return "PageList(records=" + this.getRecords() + ", total=" + this.getTotal() + ")";
    }

    public PageList(final List<T> records, final long total) {
        this.records = records;
        this.total = total;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof PageList)) {
            return false;
        } else {
            PageList<?> other = (PageList)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (!super.equals(o)) {
                return false;
            } else {
                Object this$records = this.getRecords();
                Object other$records = other.getRecords();
                if (this$records == null) {
                    if (other$records == null) {
                        return this.getTotal() == other.getTotal();
                    }
                } else if (this$records.equals(other$records)) {
                    return this.getTotal() == other.getTotal();
                }

                return false;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PageList;
    }

    public int hashCode() {
        int result = super.hashCode();
        Object $records = this.getRecords();
        result = result * 59 + ($records == null ? 43 : $records.hashCode());
        long $total = this.getTotal();
        result = result * 59 + (int)($total >>> 32 ^ $total);
        return result;
    }
}
