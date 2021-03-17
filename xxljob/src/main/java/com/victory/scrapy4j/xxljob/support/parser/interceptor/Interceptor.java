package com.victory.scrapy4j.xxljob.support.parser.interceptor;


import com.victory.scrapy4j.core.component.pojo.Request;
import com.victory.scrapy4j.core.component.pojo.Response;
import com.victory.scrapy4j.core.component.pojo.Result;

public interface Interceptor {
    boolean preHandle(Request request, Response response, Result result);

    void postHandle(Request request, Response response, Result result);

    void afterCompletion(Request request, Response response, Result result, Exception ex);
}
