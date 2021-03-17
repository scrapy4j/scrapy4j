package com.victory.scrapy4j.core.support.mybatis.core.metadata;

import com.victory.scrapy4j.core.support.mybatis.toolkit.CollectionUtils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface IPage<T> extends Serializable {
    /** @deprecated */
    @Deprecated
    default String[] descs() {
        return null;
    }

    /** @deprecated */
    @Deprecated
    default String[] ascs() {
        return null;
    }

    List<OrderItem> orders();

    default Map<Object, Object> condition() {
        return null;
    }

    default boolean optimizeCountSql() {
        return true;
    }

    default boolean isSearchCount() {
        return true;
    }

    default long offset() {
        return this.getCurrent() > 0L ? (this.getCurrent() - 1L) * this.getSize() : 0L;
    }

    default long getPages() {
        if (this.getSize() == 0L) {
            return 0L;
        } else {
            long pages = this.getTotal() / this.getSize();
            if (this.getTotal() % this.getSize() != 0L) {
                ++pages;
            }

            return pages;
        }
    }

    default IPage<T> setPages(long pages) {
        return this;
    }

    default void hitCount(boolean hit) {
    }

    default boolean isHitCount() {
        return false;
    }

    List<T> getRecords();

    IPage<T> setRecords(List<T> records);

    long getTotal();

    IPage<T> setTotal(long total);

    long getSize();

    IPage<T> setSize(long size);

    long getCurrent();

    IPage<T> setCurrent(long current);

    default <R> IPage<R> convert(Function<? super T, ? extends R> mapper) {
        List<R> collect = (List)this.getRecords().stream().map(mapper).collect(Collectors.toList());
        return (IPage<R>) this.setRecords((List<T>) collect);
    }

    default String cacheKey() {
        StringBuilder key = new StringBuilder();
        key.append(this.offset()).append(":").append(this.getSize());
        List<OrderItem> orders = this.orders();
        if (CollectionUtils.isNotEmpty(orders)) {
            Iterator var3 = orders.iterator();

            while(var3.hasNext()) {
                OrderItem item = (OrderItem)var3.next();
                key.append(":").append(item.getColumn()).append(":").append(item.isAsc());
            }
        }

        return key.toString();
    }
}
