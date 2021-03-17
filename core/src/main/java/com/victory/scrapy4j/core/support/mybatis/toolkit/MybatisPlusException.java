package com.victory.scrapy4j.core.support.mybatis.toolkit;

public class MybatisPlusException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MybatisPlusException(String message) {
        super(message);
    }

    public MybatisPlusException(Throwable throwable) {
        super(throwable);
    }

    public MybatisPlusException(String message, Throwable throwable) {
        super(message, throwable);
    }
}