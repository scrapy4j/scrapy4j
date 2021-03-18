package org.scrapy4j.core.support.feign;

import org.scrapy4j.core.pojo.Response;
import feign.RequestLine;

public interface HttpMethodTarget {
    @RequestLine("GET")
    Response get();

    @RequestLine("POST")
    Response post(Object object);
}
