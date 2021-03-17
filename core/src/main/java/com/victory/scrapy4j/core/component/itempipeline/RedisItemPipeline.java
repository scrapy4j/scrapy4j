package com.victory.scrapy4j.core.component.itempipeline;


import com.victory.scrapy4j.core.component.item.Item;
import com.victory.scrapy4j.core.component.item.RedisItem;
import com.victory.scrapy4j.core.component.spider.Spider;
import com.victory.scrapy4j.core.support.redis.metadata.RedisData;
import com.victory.scrapy4j.core.support.redis.toolkit.RedisOperator;
import com.victory.scrapy4j.core.support.redis.toolkit.RedisOperatorFactory;
import com.victory.scrapy4j.core.support.redis.toolkit.RedisService;
import com.victory.scrapy4j.core.utils.Utils;

import java.util.List;

public class RedisItemPipeline implements ItemPipeline<RedisData> {

    private RedisService redisService;

    public RedisItemPipeline(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public void processItem(List<Item<RedisData>> items, Spider spider) {
        items.forEach(item -> {
            if (support(item)) {
                RedisData data = item.values();
                try {
                    RedisOperator operator = RedisOperatorFactory.getOperator(data.getType().name()).orElseThrow(
                            () -> new IllegalArgumentException("Invalid item type")
                    );
                    operator.excute(data, redisService);
                } catch (IllegalArgumentException ex) {
                    Utils.logError(spider.getSettings().getLogger(this.getClass()), String.format("pipeline error:%s", ex.getMessage()), ex);
                }
            }
        });
    }

    @Override
    public boolean support(Item item) {
        return item instanceof RedisItem;
    }
}
