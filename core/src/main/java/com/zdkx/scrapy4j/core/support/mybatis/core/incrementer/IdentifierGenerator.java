package com.zdkx.scrapy4j.core.support.mybatis.core.incrementer;

import com.zdkx.scrapy4j.core.support.mybatis.toolkit.IdWorker;

public interface IdentifierGenerator {
    Number nextId(Object entity);

    default String nextUUID(Object entity) {
        return IdWorker.get32UUID();
    }
}