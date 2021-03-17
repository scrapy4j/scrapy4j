package com.victory.scrapy4j.core.support.mybatis.annotation;

public enum FieldStrategy {
    IGNORED,
    NOT_NULL,
    NOT_EMPTY,
    DEFAULT,
    NEVER;

    private FieldStrategy() {
    }
}