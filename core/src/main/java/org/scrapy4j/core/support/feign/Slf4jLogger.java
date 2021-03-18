package org.scrapy4j.core.support.feign;

import feign.Logger;
import feign.Request;
import feign.Response;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Slf4jLogger extends Logger {

    private final org.slf4j.Logger logger;

    public Slf4jLogger() {
        this(Logger.class);
    }

    public Slf4jLogger(Class<?> clazz) {
        this(LoggerFactory.getLogger(clazz));
    }

    public Slf4jLogger(String name) {
        this(LoggerFactory.getLogger(name));
    }

    public Slf4jLogger(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    protected void logRequest(String configKey, Level logLevel, Request request) {
//        if (this.logger.isDebugEnabled()) {
            super.logRequest(configKey, logLevel, request);
//        }

    }

    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
//        return this.logger.isDebugEnabled() ? super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime) : response;
        return super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
    }

    protected void log(String configKey, String format, Object... args) {
//        if (this.logger.isDebugEnabled()) {
            this.logger.info(String.format(methodTag(configKey) + format, args));
//        }

    }
}
