package com.zdkx.scrapy4j.core.support.mybatis.core.incrementer;

public interface IKeyGenerator {
    String executeSql(String incrementerName);
}