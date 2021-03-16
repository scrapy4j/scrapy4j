package com.zdkx.scrapy4j.core.component.downloader.middleware;

import com.zdkx.scrapy4j.core.component.pojo.Response;

public interface DownloaderMiddleware {
    Response download();
}
