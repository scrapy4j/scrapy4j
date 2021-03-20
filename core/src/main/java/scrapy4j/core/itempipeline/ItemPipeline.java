package scrapy4j.core.itempipeline;


import scrapy4j.core.item.Item;
import scrapy4j.core.spider.Spider;

import java.util.List;

/**
 * 管道：
 * 负责处理Spider中获取到的Item，并进行后期处理（详细分析，过滤，存储等）
 *
 * @Description: ItemPipeline
 * @Author: yuanxiaocong
 * @Date: 2020/12/3
 */
public interface ItemPipeline<T> {

    void processItem(List<Item<T>> items, Spider spider);

    boolean support(Item item);
}
