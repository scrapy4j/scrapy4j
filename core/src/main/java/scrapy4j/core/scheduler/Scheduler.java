package scrapy4j.core.scheduler;

import scrapy4j.core.pojo.Request;
import scrapy4j.core.pojo.Response;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 调度器(调度队列)：
 * 负责接收引擎发送过来的Request请求，并按照一定的方式进行整理排列，入队，当引擎需要时，交还给引擎
 *
 * @Description: Scheduler
 * @Author: tianj
 * @Date: 2020/12/3
 */
public class Scheduler {

    Iterator startRequests;

    private Queue<Request> requestQueue;
    private Queue<Response> responseQueue;

    public Scheduler() {
    }

    public void open(Iterator startRequests) {
        this.requestQueue = new ConcurrentLinkedDeque<>();
        this.responseQueue = new ConcurrentLinkedDeque<>();
        this.startRequests = startRequests;
    }

    public void addRequest(Request request) {
        this.requestQueue.add(request);
    }

    public boolean hasRequest() {
        return startRequests.hasNext() || !this.requestQueue.isEmpty();
    }

    public Request nextRequest() {
        if (startRequests.hasNext()) {
            requestQueue.add((Request) startRequests.next());
        }
        return requestQueue.poll();
    }

    public void addResponse(Response response) {
        this.responseQueue.add(response);
    }

    public boolean hasResponse() {
        return !responseQueue.isEmpty();
    }

    public Response nextResponse() {
        return responseQueue.poll();
    }

    public Queue<Request> getRequestQueue() {
        return requestQueue;
    }

    public void setRequestQueue(BlockingDeque<Request> requestQueue) {
        this.requestQueue = requestQueue;
    }

    public Queue<Response> getResponseQueue() {
        return responseQueue;
    }

    public void setResponseQueue(BlockingDeque<Response> responseQueue) {
        this.responseQueue = responseQueue;
    }
}
