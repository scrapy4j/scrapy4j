package com.victory.scrapy4j.core.component.downloader.middleware;

import com.victory.scrapy4j.core.component.pojo.Response;

public interface DownloaderMiddleware {
    Response download();
}
