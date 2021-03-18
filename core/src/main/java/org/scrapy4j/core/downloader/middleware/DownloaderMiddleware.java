package org.scrapy4j.core.downloader.middleware;

import org.scrapy4j.core.pojo.Response;

public interface DownloaderMiddleware {
    Response download();
}
