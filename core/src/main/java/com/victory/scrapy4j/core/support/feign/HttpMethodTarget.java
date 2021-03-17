package com.victory.scrapy4j.core.support.feign;

import com.victory.scrapy4j.core.component.pojo.Response;
import feign.RequestLine;

public interface HttpMethodTarget {
    @RequestLine("GET")
    Response get();

    @RequestLine("POST")
    Response post(Object object);
}