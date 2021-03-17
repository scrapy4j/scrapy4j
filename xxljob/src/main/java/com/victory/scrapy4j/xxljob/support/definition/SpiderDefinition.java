package com.victory.scrapy4j.xxljob.support.definition;


import com.victory.scrapy4j.core.component.pojo.Settings;

import java.util.List;

public class SpiderDefinition {

    private List<String> startUrls;

    private StartRequestsDefinition startRequests;

    private List<ItemPipelineDefinition> itemPipelines;

    private ParserDefinition parser;

    private Settings settings;

    public SpiderDefinition(){

    }

    public List<String> getStartUrls() {
        return startUrls;
    }

    public void setStartUrls(List<String> startUrls) {
        this.startUrls = startUrls;
    }

    public StartRequestsDefinition getStartRequests() {
        return startRequests;
    }

    public void setStartRequests(StartRequestsDefinition startRequests) {
        this.startRequests = startRequests;
    }

    public List<ItemPipelineDefinition> getItemPipelines() {
        return itemPipelines;
    }

    public void setItemPipelines(List<ItemPipelineDefinition> itemPipelines) {
        this.itemPipelines = itemPipelines;
    }

    public ParserDefinition getParser() {
        return parser;
    }

    public void setParser(ParserDefinition parser) {
        this.parser = parser;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
