package org.scrapy4j.core.support.mybatis.core.enums;

public enum FieldStrategy {
    IGNORED,
    NOT_NULL,
    NOT_EMPTY,
    DEFAULT,
    NEVER;

    private FieldStrategy() {
    }
}