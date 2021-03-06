package scrapy4j.core.itempipeline;


import scrapy4j.core.item.Item;
import scrapy4j.core.item.RedisItem;
import scrapy4j.core.spider.Spider;
import scrapy4j.core.support.redis.metadata.RedisData;
import scrapy4j.core.support.redis.toolkit.RedisOperator;
import scrapy4j.core.support.redis.toolkit.RedisOperatorFactory;
import scrapy4j.core.support.redis.toolkit.RedisService;
import scrapy4j.core.utils.Utils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

public class RedisItemPipeline implements ItemPipeline<RedisData> {

    private RedisService redisService;

    public RedisItemPipeline(RedisTemplate<String, String> redisTemplate) {
        this.redisService = new RedisService(redisTemplate);
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
