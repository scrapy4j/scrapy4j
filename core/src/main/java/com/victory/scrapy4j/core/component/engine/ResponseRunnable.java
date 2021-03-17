package com.victory.scrapy4j.core.component.engine;


import com.victory.scrapy4j.core.component.parser.IParser;
import com.victory.scrapy4j.core.component.pojo.Response;
import com.victory.scrapy4j.core.component.pojo.Result;
import com.victory.scrapy4j.core.component.spider.Spider;
import com.victory.scrapy4j.core.component.scheduler.Scheduler;
import com.victory.scrapy4j.core.utils.Utils;

import java.util.concurrent.atomic.AtomicInteger;

public class ResponseRunnable implements Runnable {

    Response response;
    Spider spider;
    Scheduler scheduler;
    AtomicInteger responseInProgress;

    public ResponseRunnable(Response response, AtomicInteger responseInProgress, Scheduler scheduler, Spider spider) {
        this.response = response;
        this.spider = spider;
        this.responseInProgress = responseInProgress;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        try {
            IParser parser = response.getRequest().getParser();
            Result result = new Result();
            if (parser != null) {
                result = parser.parse(response);
            }
            if (result != null && result.getRequests() != null && !result.getRequests().isEmpty()) {
                result.getRequests().forEach(request -> {
                    //propagation
                    if (request.getSpider() == null) {
                        request.setSpider(this.spider);
                    }
                    if (request.getParser() == null) {
                        request.setParser(response.getRequest().getParser());
                    }
                    scheduler.addRequest(request);
                });
                Utils.logInfo(this.response.getRequest().getSpider().getSettings().getLogger(this.getClass()), String.format("responseHandle add requests:%s url:%s", result.getRequests().size(), this.response.getRequest().getUrl()));
            }
            if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
                Result finalResult = result;
                response.getRequest().getSpider().getItemPipelines().forEach(m -> m.processItem(finalResult.getItems(), response.getRequest().getSpider()));
                Utils.logInfo(this.response.getRequest().getSpider().getSettings().getLogger(this.getClass()), String.format("responseHandle process items:%s url:%s", result.getItems().size(), this.response.getRequest().getUrl()));
            }
        } catch (Exception ex) {
            Utils.logError(this.response.getRequest().getSpider().getSettings().getLogger(this.getClass()), String.format("responseHandle error:%s %s", this.response.getRequest().getUrl(), ex.getMessage()), ex);
        } finally {
            responseInProgress.decrementAndGet();
        }
    }
}
