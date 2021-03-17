package com.victory.scrapy4j.xxljob.support.parser.interceptor;


import com.victory.scrapy4j.core.component.pojo.Request;
import com.victory.scrapy4j.core.component.pojo.Response;
import com.victory.scrapy4j.core.component.pojo.Result;

import java.util.ArrayList;
import java.util.List;

public class InterceptorChain {
    private final List<Interceptor> interceptorList = new ArrayList<>();

    private int interceptorIndex = -1;

    public void addInterceptor(Interceptor interceptor) {
        this.interceptorList.add(interceptor);
    }

    public void addInterceptors(List<Interceptor> interceptors) {
        if (interceptors != null) {
            this.interceptorList.addAll(interceptors);
        }
    }

    public boolean applyPreHandle(Request request, Response response, Result result) {
        for (int i = 0; i < this.interceptorList.size(); i++) {
            Interceptor interceptor = this.interceptorList.get(i);
            if (!interceptor.preHandle(request, response, result)) {
                triggerAfterCompletion(request, response, result, null);
                return false;
            }
            this.interceptorIndex = i;
        }
        return true;
    }

    public void applyPostHandle(Request request, Response response, Result result) {
        for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
            Interceptor interceptor = this.interceptorList.get(i);
            interceptor.postHandle(request, response, result);
        }
    }

    public void triggerAfterCompletion(Request request, Response response, Result result, Exception ex) {
        for (int i = this.interceptorIndex; i >= 0; i--) {
            Interceptor interceptor = this.interceptorList.get(i);
            try {
                interceptor.afterCompletion(request, response, result, ex);
            } catch (Throwable ex2) {

            }
        }
    }
}
