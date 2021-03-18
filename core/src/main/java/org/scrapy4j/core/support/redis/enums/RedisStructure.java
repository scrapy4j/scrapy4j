package org.scrapy4j.core.support.redis.enums;

/**
 * redis数据结构
 *
 * @Description: RedisStructure
 * @Author: yuanxiaocong
 * @Date: 2020/12/9
 */
public enum RedisStructure {
    /**
     * 字符串
     */
    string,
    /**
     * 键值对
     */
    hash,
    /**
     * 有序链表
     */
    list,
    /**
     * 集合
     */
    set,
    /**
     * 有序集合
     */
    zset
}
