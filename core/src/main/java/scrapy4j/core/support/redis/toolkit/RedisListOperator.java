package scrapy4j.core.support.redis.toolkit;

import scrapy4j.core.support.redis.metadata.RedisData;
import org.springframework.stereotype.Component;

@Component
public class RedisListOperator implements RedisOperator<String> {

    @Override
    public void excute(RedisData<String> data, RedisService redisService) {
        redisService.lpush(data.getKey(), data.getValue());
    }

}
