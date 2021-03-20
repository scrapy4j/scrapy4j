package scrapy4j.core.downloader.middleware;

import scrapy4j.core.pojo.Response;

public interface DownloaderMiddleware {
    Response download();
}
