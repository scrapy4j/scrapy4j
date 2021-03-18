package org.scrapy4j.core.support.redis.toolkit;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface RedisClient {
    Long hlen(String key);

    Long llen(String key);

    boolean hsetnx(String key, String field, String value);

    List<String> lrange(String key, long start, long end);

    Map<String, String> hgetAll(String key);

    boolean del(String key);

    Long hdel(String key, String field);

    void hset(String key, String field, String value);

    void hset(String key, Map<String, String> values);

    String hget(String key, String field);

    boolean pexpireAt(String key, Date timeout);

    Boolean expireAt(String key, Date date);

    boolean expire(String key, long timeout);

    long ttl(String key);

    Long lpush(String key, String value);

    Long rpush(String key, String value);

    String lpop(String key);

    String rpop(String key);

    String brpop(String key);

    boolean zadd(String key, double score, String value);

    Long zcount(String key, double min, double max);

    Long zremrangeByScore(String key, double min, double max);

    Set<String> zrangeByScore(String key, double min, double max);

    Set<String> zrangeByScore(String key, double min, double max, long offset, long count);

    Set<String> zrevrangeByScore(String key, double min, double max);

    Set<String> zrevrangeByScore(String key, double min, double max, long offset, long count);

    Set<String> keys(String pattern);

    Set<String> hkeys(String key);

    Set<String> smembers(String key);

    Long sadd(String key, String member);

    Long srem(String key, String member);

    Long incr(String key);

    Long incr(String key, long value);

    Boolean hasKey(String key);

    void set(String key, String value);

    void setnx(String key, String value);

    String get(String key);

    void set(String key, String value, long timeout, TimeUnit unit);
}
