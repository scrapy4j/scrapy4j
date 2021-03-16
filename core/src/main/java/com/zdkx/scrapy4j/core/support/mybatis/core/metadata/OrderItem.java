package com.zdkx.scrapy4j.core.support.mybatis.core.metadata;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String column;
    private boolean asc = true;

    public static OrderItem asc(String column) {
        return build(column, true);
    }

    public static OrderItem desc(String column) {
        return build(column, false);
    }

    public static List<OrderItem> ascs(String... columns) {
        return (List)Arrays.stream(columns).map(OrderItem::asc).collect(Collectors.toList());
    }

    public static List<OrderItem> descs(String... columns) {
        return (List)Arrays.stream(columns).map(OrderItem::desc).collect(Collectors.toList());
    }

    private static OrderItem build(String column, boolean asc) {
        OrderItem item = new OrderItem();
        item.setColumn(column);
        item.setAsc(asc);
        return item;
    }

    public OrderItem() {
    }

    public String getColumn() {
        return this.column;
    }

    public boolean isAsc() {
        return this.asc;
    }

    public OrderItem setColumn(final String column) {
        this.column = column;
        return this;
    }

    public OrderItem setAsc(final boolean asc) {
        this.asc = asc;
        return this;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof OrderItem)) {
            return false;
        } else {
            OrderItem other = (OrderItem)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$column = this.getColumn();
                Object other$column = other.getColumn();
                if (this$column == null) {
                    if (other$column == null) {
                        return this.isAsc() == other.isAsc();
                    }
                } else if (this$column.equals(other$column)) {
                    return this.isAsc() == other.isAsc();
                }

                return false;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof OrderItem;
    }

    public int hashCode() {
        int result = 1;
        Object $column = this.getColumn();
        result = result * 59 + ($column == null ? 43 : $column.hashCode());
        result = result * 59 + (this.isAsc() ? 79 : 97);
        return result;
    }

    public String toString() {
        return "OrderItem(column=" + this.getColumn() + ", asc=" + this.isAsc() + ")";
    }
}
