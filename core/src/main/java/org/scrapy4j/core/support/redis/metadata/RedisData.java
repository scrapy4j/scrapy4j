package org.scrapy4j.core.support.redis.metadata;


import org.scrapy4j.core.support.redis.enums.RedisStructure;

import java.util.concurrent.TimeUnit;

public class RedisData<T> {
    private RedisStructure type;
    private String key;
    private T value;
    /**
     * hash结构field
     */
    private String field;
    /**
     * zset权重参数
     */
    private double score;
    private Long timeout;
    private TimeUnit timeUnit;

    public RedisStructure getType() {
        return type;
    }

    public void setType(RedisStructure type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
