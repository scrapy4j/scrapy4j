package scrapy4j.core.support.redis.toolkit;

import scrapy4j.core.support.redis.metadata.RedisData;
import org.springframework.stereotype.Component;

@Component
public class RedisHashOperator implements RedisOperator<String> {

    @Override
    public void excute(RedisData<String> data, RedisService redisService) {
        redisService.hset(data.getKey(), data.getField(), data.getValue());
    }

}
