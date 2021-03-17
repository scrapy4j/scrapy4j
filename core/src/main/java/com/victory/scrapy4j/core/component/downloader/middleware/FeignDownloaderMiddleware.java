package com.victory.scrapy4j.core.component.downloader.middleware;

import com.alibaba.fastjson.JSONObject;
import com.victory.scrapy4j.core.component.scheduler.Scheduler;
import com.victory.scrapy4j.core.support.feign.*;
import com.victory.scrapy4j.core.utils.Utils;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;

import java.util.Map;

/**
 * 基于fegin的下载器
 *
 * @Description: FeignDownloader
 * @Author: yuanxiaocong
 * @Date: 2021/3/16
 */
public class FeignDownloaderMiddleware implements DownloaderMiddleware {
    private com.victory.scrapy4j.core.component.pojo.Request request;
    private Scheduler scheduler;
    private FeignSettings settings;

    public FeignDownloaderMiddleware(Scheduler scheduler, com.victory.scrapy4j.core.component.pojo.Request request, FeignSettings settings) {
        this.scheduler = scheduler;
        this.request = request;
        this.settings = settings;
    }

    public FeignDownloaderMiddleware(com.victory.scrapy4j.core.component.pojo.Request request, FeignSettings settings) {
        this.request = request;
        this.settings = settings;
    }

    @Override
    public com.victory.scrapy4j.core.component.pojo.Response download() {
        com.victory.scrapy4j.core.component.pojo.Response response = new com.victory.scrapy4j.core.component.pojo.Response();
        try {
            Feign.Builder builder = Feign.builder()
                    .client(new Client.Default(null, null))
                    .contract(new Contract.Default())
                    .logger(settings.getLogger() != null
                            ? new Slf4jLogger(settings.getLogger())
                            : new feign.slf4j.Slf4jLogger(HttpMethodTarget.class))
                    .encoder(settings.getEncoder() != null ? settings.getEncoder() : new Encoder.Default())
                    .decoder(new FeignDecoder(settings.getDecoder() != null ? settings.getDecoder() : new Decoder.Default()))
                    .logLevel(settings.getLogLevel() != null ? settings.getLogLevel() : Logger.Level.FULL)
                    .retryer(settings.getRetryTimes() == 0
                            ? Retryer.NEVER_RETRY
                            : new Retryer.Default(settings.getDownloadDelayMillis(), settings.getDownloadDelayMillis(), settings.getRetryTimes()))
                    .options(new feign.Request.Options(this.settings.getConnectTimeoutSecs() * 1000, this.settings.getDownloadTimeoutSecs() * 1000, true));
            builder.requestInterceptor(new RequestTemplateResolveInterceptor(this.request));
            if (settings.getRequestInterceptors() != null && !settings.getRequestInterceptors().isEmpty()) {

                settings.getRequestInterceptors().forEach(builder::requestInterceptor);
            }
            HttpMethodTarget target = builder.target(HttpMethodTarget.class, "empty");
            if (this.request.getHttpMethod().equals(feign.Request.HttpMethod.GET)) {
                response = target.get();
            } else if (this.request.getHttpMethod().equals(feign.Request.HttpMethod.POST)) {
                com.victory.scrapy4j.core.component.pojo.Request.RequestBuilder requestBuilder = this.request.toBuilder();
                /**
                 * body resolve
                 * TODO replace to bodyTemplate?
                 */
                Object body = this.request.getRequestBody().getObject();
                if (body != null) {
                    Map<String, Object> variables = this.request.getVariables();
                    if (variables != null) {
                        //为了避免后面headers queries使用的 variables参数发生改变（当有function等lambda表达式动态执行时），这里body使用时就处理成固定值
                        this.request.setResolvedVariables(Utils.mapResolve(variables));
                    }
                    if (body instanceof Map) {
                        Map<String, Object> bodyMap = (Map<String, Object>) body;
                        Map<String, Object> targetMap = Utils.mapResolve(bodyMap);
                        for (String key : targetMap.keySet()) {
                            if (targetMap.get(key) instanceof String) {
                                targetMap.put(key, Utils.formatVars(targetMap.get(key).toString(), this.request.getResolvedVariables()));
                            }
                        }
                        requestBuilder.requestBody(() -> targetMap);
                    }
                    if (body instanceof JSONObject) {
                        Map<String, Object> bodyMap = ((JSONObject) body).getInnerMap();
                        Map<String, Object> targetMap = Utils.mapResolve(bodyMap);
                        for (String key : targetMap.keySet()) {
                            if (targetMap.get(key) instanceof String) {
                                targetMap.put(key, Utils.formatVars(targetMap.get(key).toString(), this.request.getResolvedVariables()));
                            }
                        }
                        requestBuilder.requestBody(() -> new JSONObject(targetMap));
                    }
                    if (body instanceof String) {
                        body = Utils.formatVars(body.toString(), this.request.getResolvedVariables());
                        Object finalBody = body;
                        requestBuilder.requestBody(() -> finalBody);
                    }
                }
                response = target.post(requestBuilder.build().getRequestBody().getObject());
            }

        } catch (Exception ex) {
            Utils.logError(settings.getLogger(FeignDownloaderMiddleware.class), String.format("feignMiddleware error:%s", this.request.getUrl()), ex);
        }
        response.setRequest(this.request);
        return response;
    }
}
