package com.victory.scrapy4j.core.support.mybatis.core.incrementer;

public interface IKeyGenerator {
    String executeSql(String incrementerName);
}