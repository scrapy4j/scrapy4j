package org.scrapy4j.core.support.redis.toolkit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RedisService implements RedisClient {

    RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long hlen(String key) {
        return this.redisTemplate.opsForHash().size(key);
    }

    public Long llen(String key) {
        return this.redisTemplate.opsForList().size(key);
    }

    public boolean hsetnx(String key, String field, String value) {
        return this.redisTemplate.opsForHash().putIfAbsent(key, field, value);
    }

    public List<String> lrange(String key, long start, long end) {
        return this.redisTemplate.opsForList().range(key, start, end);
    }

    public Map<String, String> hgetAll(String key) {
        Map<String, String> map = new HashMap();
        Map<Object, Object> entries = this.redisTemplate.opsForHash().entries(key);
        Iterator var4 = entries.keySet().iterator();

        while(var4.hasNext()) {
            Object field = var4.next();
            map.put((String)field, (String)entries.get(field));
        }

        return map;
    }

    public boolean del(String key) {
        return this.redisTemplate.delete(key);
    }

    public Long hdel(String key, String field) {
        return this.redisTemplate.opsForHash().delete(key, new Object[]{field});
    }

    public void hset(String key, String field, String value) {
        this.redisTemplate.opsForHash().put(key, field, value);
    }

    public void hset(String key, Map<String, String> values) {
        this.redisTemplate.opsForHash().putAll(key, values);
    }

    public String hget(String key, String field) {
        return (String)this.redisTemplate.opsForHash().get(key, field);
    }

    public boolean pexpireAt(String key, Date timeout) {
        return this.redisTemplate.expireAt(key, timeout);
    }

    public Boolean expireAt(String key, Date date) {
        RedisSerializer<String> series = (RedisSerializer<String>) this.redisTemplate.getKeySerializer();
        byte[] rawKey = series.serialize(key);
        boolean flag = (Boolean)this.redisTemplate.execute((connection) -> {
            return connection.expireAt(rawKey, date.getTime() / 1000L);
        }, true);
        return flag;
    }

    public boolean expire(String key, long timeout) {
        return this.redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    public long ttl(String key) {
        return this.redisTemplate.getExpire(key);
    }

    public Long lpush(String key, String value) {
        return this.redisTemplate.opsForList().leftPush(key, value);
    }

    public Long rpush(String key, String value) {
        return this.redisTemplate.opsForList().rightPush(key, value);
    }

    public String lpop(String key) {
        return (String)this.redisTemplate.opsForList().leftPop(key);
    }

    public String rpop(String key) {
        return (String)this.redisTemplate.opsForList().rightPop(key);
    }

    public String brpop(String key) {
        return (String)this.redisTemplate.opsForList().rightPop(key, 0L, TimeUnit.MILLISECONDS);
    }

    public boolean zadd(String key, double score, String value) {
        return this.redisTemplate.opsForZSet().add(key, value, score);
    }

    public Long zcount(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().count(key, min, max);
    }

    public Long zremrangeByScore(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    public Set<String> zrangeByScore(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public Set<String> zrangeByScore(String key, double min, double max, long offset, long count) {
        return this.redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
    }

    public Set<String> zrevrangeByScore(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
    }

    public Set<String> zrevrangeByScore(String key, double min, double max, long offset, long count) {
        return this.redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
    }

    public Set<String> keys(String pattern) {
        Set<String> set = new HashSet();
        Set<String> keys = this.redisTemplate.keys(pattern);
        Iterator var4 = keys.iterator();

        while(var4.hasNext()) {
            Object obj = var4.next();
            set.add((String)obj);
        }

        return set;
    }

    public Set<String> hkeys(String key) {
        Set<String> set = new HashSet();
        Set<Object> keys = this.redisTemplate.opsForHash().keys(key);
        Iterator var4 = keys.iterator();

        while(var4.hasNext()) {
            Object obj = var4.next();
            set.add((String)obj);
        }

        return set;
    }

    public Set<String> smembers(String key) {
        return this.redisTemplate.opsForSet().members(key);
    }

    public Long sadd(String key, String member) {
        return this.redisTemplate.opsForSet().add(key, new String[]{member});
    }

    public Long srem(String key, String member) {
        return this.redisTemplate.opsForSet().remove(key, new Object[]{member});
    }

    public Long incr(String key) {
        return this.redisTemplate.opsForValue().increment(key, 1L);
    }

    public Long incr(String key, long value) {
        return this.redisTemplate.opsForValue().increment(key, value);
    }

    public Boolean hasKey(String key) {
        return this.redisTemplate.hasKey(key);
    }

    public void set(String key, String value) {
        this.redisTemplate.opsForValue().set(key, value);
    }

    public void setnx(String key, String value) {
        this.redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public String get(String key) {
        return (String)this.redisTemplate.opsForValue().get(key);
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        this.redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
}
