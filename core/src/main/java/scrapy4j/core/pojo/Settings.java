package scrapy4j.core.pojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings {
    private int concurrentRequests;
    private long downloadDelayMillis;
    private int downloadTimeoutSecs;
    private int retryTimes;
    private transient Logger logger;


    public Settings() {
        this.retryTimes = 0;
        this.concurrentRequests = 16;
        this.downloadDelayMillis = 0;
        this.downloadTimeoutSecs = 60;
    }


    public long getDownloadDelayMillis() {
        return downloadDelayMillis;
    }

    public void setDownloadDelayMillis(long downloadDelayMillis) {
        this.downloadDelayMillis = downloadDelayMillis;
    }

    public int getDownloadTimeoutSecs() {
        return downloadTimeoutSecs;
    }

    public void setDownloadTimeoutSecs(int downloadTimeoutSecs) {
        this.downloadTimeoutSecs = downloadTimeoutSecs;
    }

    public int getConcurrentRequests() {
        return concurrentRequests;
    }

    public void setConcurrentRequests(int concurrentRequests) {
        this.concurrentRequests = concurrentRequests;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public org.slf4j.Logger getLogger(){
        return this.logger;
    }

    public org.slf4j.Logger getLogger(Class defaultClazz) {
        return logger!=null?logger: LoggerFactory.getLogger(defaultClazz);
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
