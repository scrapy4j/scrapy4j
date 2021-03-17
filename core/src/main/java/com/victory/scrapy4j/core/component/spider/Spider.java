package com.victory.scrapy4j.core.component.spider;

import com.victory.scrapy4j.core.component.pojo.Request;
import com.victory.scrapy4j.core.component.pojo.Settings;
import com.victory.scrapy4j.core.component.itempipeline.ItemPipeline;
import com.victory.scrapy4j.core.component.parser.Parser;

import java.util.ArrayList;
import java.util.List;

/**
 * 爬虫：
 * 负责处理所有Response,从中分析提取数据，获取Item字段需要的数据，并将需要跟进的URL提交给引擎，再次进入Scheduler（调度器）
 *
 * @Description: Spider
 * @Author: tianj
 * @Date: 2020/12/3
 */
public class Spider {
    private final StartRequests startRequests;
    private final List<String> startUrls;
    private final List<ItemPipeline> itemPipelines;
    private final Parser parser;
    private final Settings settings;

    private Spider(SpiderBuilder spiderBuilder) {
        this.startUrls = spiderBuilder.startUrls;
        this.settings = spiderBuilder.settings;
        this.startRequests = spiderBuilder.startRequests;
        this.parser = spiderBuilder.parser;
        this.itemPipelines = spiderBuilder.itemPipelines;
    }

    public static SpiderBuilder builder() {
        return new SpiderBuilder();
    }

    public SpiderBuilder toBuilder() {
        return new SpiderBuilder()
                .startRequests(this.startRequests)
                .startUrls(this.startUrls)
                .parser(this.parser)
                .itemPipelines(this.itemPipelines)
                .settings(this.settings);
    }

    public StartRequests getStartRequests() {
        return startRequests;
    }

    public List<String> getStartUrls() {
        return startUrls;
    }

    public List<ItemPipeline> getItemPipelines() {
        return itemPipelines;
    }

    public Parser getParser() {
        return parser;
    }

    public Settings getSettings() {
        return settings;
    }

    public static final class SpiderBuilder {
        private StartRequests startRequests = new StartRequests();
        private List<String> startUrls;
        private List<ItemPipeline> itemPipelines;
        private Parser parser;
        private Settings settings;

        public SpiderBuilder() {
        }

        public SpiderBuilder startRequests(StartRequests startRequests) {
            this.startRequests = startRequests;
            return this;
        }

        public SpiderBuilder addStartRequest(Request request) {
            this.startRequests.addRequest(request);
            return this;
        }

        public SpiderBuilder addStartRequests(List<Request> requests) {
            this.startRequests.addRequests(requests);
            return this;
        }

        public SpiderBuilder startUrls(List<String> startUrls) {
            this.startUrls = startUrls;
            return this;
        }

        public SpiderBuilder itemPipelines(List<ItemPipeline> itemPipelines) {
            this.itemPipelines = itemPipelines;
            return this;
        }

        public SpiderBuilder addItemPipelines(ItemPipeline itemPipeline) {
            if (itemPipelines == null) {
                itemPipelines = new ArrayList<>();
            }
            itemPipelines.add(itemPipeline);
            return this;
        }


        public SpiderBuilder parser(Parser parser) {
            this.parser = parser;
            return this;
        }

        public SpiderBuilder settings(Settings settings) {
            this.settings = settings;
            return this;
        }

        public Spider build() {
            // check startRequest and startUrl
            if (this.startUrls == null && this.startRequests == null) {
                throw new RuntimeException("Spider's startUrl and startRequests undefined");
            }

            // check itemPipeline
            if (this.itemPipelines == null) {
                throw new RuntimeException("Spider's itemPipeline undefined");
            }

            // check parser
            if (this.parser== null) {
                throw new RuntimeException("Spider's parser undefined");
            }
            return new Spider(this);
        }
    }
}
