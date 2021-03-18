package org.scrapy4j.core.item;


import org.scrapy4j.core.support.redis.metadata.RedisData;

public class RedisItem implements Item<RedisData> {

    private RedisData redisData;

    @Override
    public RedisData values() {
        return redisData;
    }

    @Override
    public void setValues(RedisData redisData) {
        this.redisData = redisData;
    }
}
