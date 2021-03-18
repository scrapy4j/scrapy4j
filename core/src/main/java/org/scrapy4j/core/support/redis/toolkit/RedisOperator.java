package org.scrapy4j.core.support.redis.toolkit;

import org.scrapy4j.core.support.redis.metadata.RedisData;

public interface RedisOperator<T> {
    void excute(RedisData<T> data, RedisService redisService);
}
