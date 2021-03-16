package com.zdkx.scrapy4j.core.support.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.LinkedHashSet;
import java.util.Map;

public class RequestHeaderInterceptor implements RequestInterceptor {

    Map<String, LinkedHashSet<String>> headers;

    public RequestHeaderInterceptor(Map<String, LinkedHashSet<String>> headers) {
        this.headers = headers;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (headers != null && !headers.isEmpty()) {
            for (String key : headers.keySet()) {
                template.header(key, headers.get(key));
            }
        }
    }
}
