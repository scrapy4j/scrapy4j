package org.scrapy4j.core.support.redis.toolkit;

import org.scrapy4j.core.support.redis.metadata.RedisData;
import org.springframework.stereotype.Component;

@Component
public class RedisZsetOperator implements RedisOperator<String> {
    @Override
    public void excute(RedisData<String> data, RedisService redisService) {
        redisService.zadd(data.getKey(), data.getScore(), data.getValue());
    }

}
