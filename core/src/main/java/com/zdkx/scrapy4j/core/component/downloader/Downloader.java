package com.zdkx.scrapy4j.core.component.downloader;

import com.zdkx.scrapy4j.core.component.downloader.middleware.FeignDownloaderMiddleware;
import com.zdkx.scrapy4j.core.component.pojo.Request;
import com.zdkx.scrapy4j.core.component.pojo.Response;
import com.zdkx.scrapy4j.core.component.scheduler.Scheduler;
import com.zdkx.scrapy4j.core.component.spider.Spider;
import com.zdkx.scrapy4j.core.support.feign.FeignSettings;
import com.zdkx.scrapy4j.core.utils.Utils;

import java.util.concurrent.Semaphore;

/**
 * 下载器
 *
 * @Description: Downloader
 * @Author: yuanxiaocong
 * @Date: 2021/3/16
 */
public class Downloader implements Runnable {
    private Request request;
    private Scheduler scheduler;
    private Spider spider;
    private Semaphore downloadInProgress;

    public Downloader(Request request, Semaphore downloadInProgress, Scheduler scheduler, Spider spider) {
        this.spider = spider;
        this.scheduler = scheduler;
        this.request = request;
        this.downloadInProgress = downloadInProgress;
    }

    @Override
    public void run() {
        Response response;
        try {
            if (request.getSpider().getSettings() instanceof FeignSettings) {
                response = new FeignDownloaderMiddleware(scheduler, request, (FeignSettings) request.getSpider().getSettings()).download();
            } else {
                throw new Exception("FeignSettings required");
            }
            //        callback.call(res);
            if (response.getBody() == null) {
                Utils.logInfo(this.spider.getSettings().getLogger(this.getClass()), "empty response ignored");
            } else {
                this.scheduler.addResponse(response);
            }
        } catch (Exception ex) {
            Utils.logError(this.request.getSpider().getSettings().getLogger(this.getClass()), String.format("download error:%s %s", this.request.getUrl(), ex.getMessage()), ex);
        } finally {
            downloadInProgress.release();
        }
    }
}
