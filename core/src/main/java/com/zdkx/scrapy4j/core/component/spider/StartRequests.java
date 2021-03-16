package com.zdkx.scrapy4j.core.component.spider;

import com.zdkx.scrapy4j.core.component.pojo.Request;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class StartRequests extends Request implements Iterator {

    private Queue<Request> requests = new LinkedBlockingQueue<>();

    public StartRequests(RequestBuilder builder) {
        super(builder);
    }

    public StartRequests() {

    }

    @Override
    public boolean hasNext() {
        return !requests.isEmpty();
    }

    @Override
    public Object next() {
        Request request = requests.poll();
        RequestBuilder requestBuilder = request.toBuilder();
        return requestBuilder.combine(this);
    }

    public void addRequest(Request request) {
        if (StringUtils.isBlank(request.getUrl())
                || request.getHttpMethod() == null) {
            throw new RuntimeException("invalid args");
        }
        this.requests.add(request);
    }

    public void addRequests(List<Request> requests) {
        requests.forEach(m -> this.requests.add(m));
    }
}
