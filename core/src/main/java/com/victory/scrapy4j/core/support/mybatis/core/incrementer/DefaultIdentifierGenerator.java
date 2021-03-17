package com.victory.scrapy4j.core.support.mybatis.core.incrementer;


import com.victory.scrapy4j.core.support.mybatis.toolkit.Sequence;

public class DefaultIdentifierGenerator implements IdentifierGenerator {
    private final Sequence sequence;

    public DefaultIdentifierGenerator() {
        this.sequence = new Sequence();
    }

    public DefaultIdentifierGenerator(long workerId, long dataCenterId) {
        this.sequence = new Sequence(workerId, dataCenterId);
    }

    public DefaultIdentifierGenerator(Sequence sequence) {
        this.sequence = sequence;
    }

    public Long nextId(Object entity) {
        return this.sequence.nextId();
    }
}
