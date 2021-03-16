package com.zdkx.scrapy4j.core.component.item;

public interface Item<T> {
    T values();
    void setValues(T t);
}