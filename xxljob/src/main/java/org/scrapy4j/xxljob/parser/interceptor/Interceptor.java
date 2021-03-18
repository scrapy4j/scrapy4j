package org.scrapy4j.xxljob.parser.interceptor;


import org.scrapy4j.core.pojo.Request;
import org.scrapy4j.core.pojo.Response;
import org.scrapy4j.core.pojo.Result;

public interface Interceptor {
    boolean preHandle(Request request, Response response, Result result);

    void postHandle(Request request, Response response, Result result);

    void afterCompletion(Request request, Response response, Result result, Exception ex);
}
