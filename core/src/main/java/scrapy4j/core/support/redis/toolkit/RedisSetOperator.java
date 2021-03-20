package scrapy4j.core.support.redis.toolkit;

import scrapy4j.core.support.redis.metadata.RedisData;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisSetOperator implements RedisOperator<String> {

    public void excute(RedisData<String> data, RedisService redisService) {
        Long timeout = data.getTimeout();
        TimeUnit timeUnit = data.getTimeUnit();
        if (timeout != null && timeUnit != null) {
            redisService.set(data.getKey(), data.getValue(), timeout, timeUnit);
        } else {
            redisService.set(data.getKey(), data.getValue());
        }
    }
}
