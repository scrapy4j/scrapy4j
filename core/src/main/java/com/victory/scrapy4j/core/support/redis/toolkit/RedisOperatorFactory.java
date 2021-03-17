package com.victory.scrapy4j.core.support.redis.toolkit;


import com.victory.scrapy4j.core.support.redis.toolkit.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class RedisOperatorFactory {

    static Map<String, RedisOperator> operatorMap = new HashMap<>();

    static {
        operatorMap.put("string", new RedisStringOperator());
        operatorMap.put("hash", new RedisHashOperator());
        operatorMap.put("list", new RedisListOperator());
        operatorMap.put("set", new RedisSetOperator());
        operatorMap.put("zset", new RedisZsetOperator());
    }

    public static Optional<RedisOperator> getOperator(String structure) {
        return Optional.ofNullable(operatorMap.get(structure));
    }
}
