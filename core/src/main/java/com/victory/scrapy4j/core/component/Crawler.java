package com.victory.scrapy4j.core.component;


import com.victory.scrapy4j.core.component.engine.Engine;
import com.victory.scrapy4j.core.component.pojo.Request;
import com.victory.scrapy4j.core.component.pojo.Settings;
import com.victory.scrapy4j.core.component.spider.Spider;
import com.victory.scrapy4j.core.component.spider.StartRequests;

import java.util.ArrayList;
import java.util.List;


public class Crawler {
    private boolean crawling = false;

    private Spider spider;
    private Settings settings;

    private Crawler(CrawlerBuilder builder) {
        this.spider = builder.spider;
        this.settings = builder.settings;
    }

    public static CrawlerBuilder builder() {
        return new CrawlerBuilder();
    }

    public void crawl() throws Exception {
        if (this.crawling) {
            throw new RuntimeException("Crawling already taking place");
        }
        this.crawling = true;

        Spider.SpiderBuilder spiderBuilder = this.spider.toBuilder();
        //TODO merge settings
        this.spider = spiderBuilder.settings(this.settings).build();

        if (this.spider.getStartUrls() != null && !this.spider.getStartUrls().isEmpty()) {
            List<Request> requests = new ArrayList<>();
            this.spider.getStartUrls().forEach(m -> requests.add(
                    Request.builder(m)
                            .spider(this.spider)
                            .parser(this.spider.getParser())
                            .build()
            ));
            spiderBuilder.addStartRequests(requests);
        }

        StartRequests startRequests = this.spider.getStartRequests();
        if (startRequests != null) {
            startRequests.setSpider(this.spider);
            if (startRequests.getParser() == null && this.spider.getParser() != null) {
                startRequests.setParser(this.spider.getParser());
            }
        }
        spiderBuilder.startRequests(startRequests);

        new Engine().openSpider(this.spider);
        this.crawling = false;
    }

    public Spider getSpider() {
        return spider;
    }

    public Settings getSettings() {
        return settings;
    }

    public static final class CrawlerBuilder {
        private Spider spider;
        private Settings settings;

        private CrawlerBuilder() {
        }

        public CrawlerBuilder spider(Spider spider) {
            this.spider = spider;
            return this;
        }

        public CrawlerBuilder settings(Settings settings) {
            this.settings = settings;
            return this;
        }

        public Crawler build() {
            return new Crawler(this);
        }
    }
}
