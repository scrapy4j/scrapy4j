package com.victory.scrapy4j.core.support.redis.toolkit;

import com.victory.scrapy4j.core.support.redis.metadata.RedisData;

public interface RedisOperator<T> {
    void excute(RedisData<T> data, RedisService redisService);
}
