package org.scrapy4j.xxljob.definition;

import java.util.Map;

public class CrawlerDefinition {

    Map<String,Object> spider;

    private Map<String, Object> settings;

    public CrawlerDefinition() {

    }

    public Map<String, Object> getSpider() {
        return spider;
    }

    public void setSpider(Map<String, Object> spider) {
        this.spider = spider;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }
}
