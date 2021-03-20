package scrapy4j.core.engine;

import scrapy4j.core.downloader.Downloader;
import scrapy4j.core.pojo.Request;
import scrapy4j.core.pojo.Response;
import scrapy4j.core.scheduler.Scheduler;
import scrapy4j.core.spider.Spider;
import scrapy4j.core.utils.Utils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scrapy Engine(引擎)
 * 负责Spider、ItemPipeline、Downloader、Scheduler中间的通讯、信号、数据传递等
 *
 * @Description: Engine
 * @Author: tianj
 * @Date: 2020/12/3
 */
public class Engine {

    private boolean running = false;
    private boolean destroy = false;
    private Scheduler scheduler;

    Thread downloadTread;
    Thread responseTread;
    private Spider spider;
    private ExecutorService downloadExecutorService;
    private ExecutorService responseExecutorService;
    // 控制并发访问数
    private Semaphore downloadInProgress;
    private AtomicInteger responseInProgress;
    private AtomicInteger totalRequests;
//    private AtomicInteger totalRequestSucceed;
//    private AtomicInteger totalResponses;
//    private AtomicInteger totalResponseSucceed;

    public Engine() {
    }

    public void openSpider(Spider spider) throws Exception {
        // 计时器
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        this.spider = spider;
        this.downloadInProgress = new Semaphore(this.spider.getSettings().getConcurrentRequests());
        this.responseInProgress = new AtomicInteger();
        this.totalRequests = new AtomicInteger();

        // 初始化队列，并将请求入队
        this.scheduler = new Scheduler();
        this.scheduler.open(this.spider.getStartRequests());
        ThreadGroup threadGroup = new ThreadGroup("engineThreads");

        // 下载器执行线程池
        downloadExecutorService = Executors.newFixedThreadPool(this.spider.getSettings().getConcurrentRequests(), new NamedThreadFactory(threadGroup, "download"));
        responseExecutorService = Executors.newFixedThreadPool(this.spider.getSettings().getConcurrentRequests(), new NamedThreadFactory(threadGroup, "response"));

        downloadTread = new Thread(threadGroup, this::nextRequest);
        downloadTread.setName("downloadMainThread");
        downloadTread.start();

        responseTread = new Thread(threadGroup, this::nextResponse);
        responseTread.setName("responseMainThread");
        responseTread.start();

        downloadTread.join();
        responseTread.join();

        if (this.downloadExecutorService != null
                && !this.downloadExecutorService.isShutdown()) {
            this.downloadExecutorService.shutdown();
            Utils.logInfo(this.spider.getSettings().getLogger(this.getClass()), "downloadThreadPool destroyed");
        }
        if (this.responseExecutorService != null
                && !this.responseExecutorService.isShutdown()) {
            this.responseExecutorService.shutdown();
            Utils.logInfo(this.spider.getSettings().getLogger(this.getClass()), "responseThreadPool destroyed");
        }

        stopWatch.stop();
        Utils.logInfo(this.spider.getSettings().getLogger(this.getClass()), String.format("total requests:%s cost:%s ms", totalRequests.get(), stopWatch.getTime()));
    }

    private void nextRequest() {
        if (!this.scheduler.hasRequest()) {
            return;
        }
        while (this.downloading() || this.responseProcessing()) {
            if (this.scheduler.hasRequest() && this.downloadInProgress.availablePermits() > 0) {
                try {
                    downloadInProgress.acquire();
                    Request req = this.scheduler.nextRequest();
                    totalRequests.incrementAndGet();
                    this.downloadExecutorService.submit(new Downloader(req, this.downloadInProgress, this.scheduler, this.spider));
                    Utils.logInfo(this.spider.getSettings().getLogger(this.getClass()), String.format("download processing,request pending:%s,waiting:%s", pendingRequests(), this.scheduler.getRequestQueue().size()));
                    Utils.sleepMillis(this.spider.getSettings().getDownloadDelayMillis());
                } catch (InterruptedException ex) {
                    Utils.logError(this.spider.getSettings().getLogger(this.getClass()), "download error", ex);
                    downloadInProgress.release();
                }
            }
        }
        Utils.logInfo(this.spider.getSettings().getLogger(this.getClass()), "download process complete");
    }

    private boolean downloading() {
        return scheduler.hasRequest() || pendingRequests() > 0;
    }

    private int pendingRequests() {
        return this.spider.getSettings().getConcurrentRequests() - this.downloadInProgress.availablePermits();
    }

    private int pendingResponses() {
        return this.responseInProgress.get();
    }

    private boolean responseProcessing() {
        return this.scheduler.hasResponse() || pendingResponses() > 0;
    }

    private void nextResponse() {
        while (this.downloading() || this.responseProcessing()) {
            if (this.scheduler.hasResponse()) {
                Utils.logInfo(this.spider.getSettings().getLogger(this.getClass()), String.format("response processing,request pending:%s,waiting:%s,response waiting:%s", pendingRequests(), this.scheduler.getRequestQueue().size(), this.scheduler.getResponseQueue().size()));
                this.responseInProgress.incrementAndGet();
                Response response = scheduler.nextResponse();
                this.responseExecutorService.submit(new ResponseRunnable(response, responseInProgress, this.scheduler, this.spider));
            }
        }
        Utils.logInfo(this.spider.getSettings().getLogger(this.getClass()), "response process complete");
    }
}
