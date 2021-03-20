package scrapy4j.core.support.feign;

import scrapy4j.core.pojo.Settings;
import scrapy4j.core.utils.Utils;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class FeignSettings extends Settings {
    private final List<RequestInterceptor> requestInterceptors;
    private final Logger.Level logLevel;
    private final int connectTimeoutSecs;
    private Encoder encoder;
    private Decoder decoder;

    private FeignSettings(FeignSettingsBuilder builder) {
        this.setConcurrentRequests(builder.concurrentRequests);
        this.setDownloadDelayMillis(builder.downloadDelayMillis);
        this.setDownloadTimeoutSecs(builder.downloadTimeoutSecs);
        this.setRetryTimes(builder.retryTimes);
        this.setLogger(builder.logger);
        this.connectTimeoutSecs = builder.connectTimeoutSecs;
        // TODO generate requestInterceptors
        this.requestInterceptors = builder.requestInterceptors;
//        this.decoder = BeanLoader.findBean(FeignContext.class).getInstance("spider-job-feign", Decoder.class);
//        this.encoder = BeanLoader.findBean(FeignContext.class).getInstance("spider-job-feign", Encoder.class);
        if (StringUtils.isBlank(builder.logLevel)) {
            builder.logLevel = Logger.Level.FULL.name();
        }
        this.logLevel = Logger.Level.valueOf(StringUtils.upperCase(builder.logLevel));
    }

    public static FeignSettingsBuilder builder() {
        return new FeignSettingsBuilder();
    }

    public static FeignSettingsBuilder builder(Map<String, Object> settingMap) {
        return new FeignSettingsBuilder(settingMap);
    }

    public static class FeignSettingsBuilder {
        private int connectTimeoutSecs = 10;
        private int concurrentRequests = 16;
        private long downloadDelayMillis = 0;
        private int downloadTimeoutSecs = 60;
        private int retryTimes = 0;
        private String logLevel = Logger.Level.FULL.name();
        private org.slf4j.Logger logger;

        private List<RequestInterceptor> requestInterceptors;

        public FeignSettingsBuilder() {
        }

        public FeignSettingsBuilder(Map<String, Object> settingMap) {
            Utils.setFieldValue(settingMap, this);
        }

        public FeignSettingsBuilder connectTimeoutSecs(int connectTimeoutSecs) {
            this.connectTimeoutSecs = connectTimeoutSecs;
            return this;
        }

        public FeignSettingsBuilder concurrentRequests(int concurrentRequests) {
            this.concurrentRequests = concurrentRequests;
            return this;
        }

        public FeignSettingsBuilder downloadDelayMillis(long downloadDelayMillis) {
            this.downloadDelayMillis = downloadDelayMillis;
            return this;
        }

        public FeignSettingsBuilder downloadTimeoutSecs(int downloadTimeoutSecs) {
            this.downloadTimeoutSecs = downloadTimeoutSecs;
            return this;
        }

        public FeignSettingsBuilder retryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public FeignSettingsBuilder logLevel(String logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public FeignSettingsBuilder requestInterceptors(List<RequestInterceptor> requestInterceptors) {
            this.requestInterceptors = requestInterceptors;
            return this;
        }

        public FeignSettingsBuilder logger(org.slf4j.Logger logger) {
            this.logger = logger;
            return this;
        }

        public FeignSettings build() {
            return new FeignSettings(this);
        }
    }

    public List<RequestInterceptor> getRequestInterceptors() {
        return requestInterceptors;
    }

    public Logger.Level getLogLevel() {
        return logLevel;
    }

    public int getConnectTimeoutSecs() {
        return connectTimeoutSecs;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    public Decoder getDecoder() {
        return decoder;
    }
}
