package org.scrapy4j.core.pojo;

import org.scrapy4j.core.item.Item;
import java.util.ArrayList;
import java.util.List;

/**
 * 请求处理结果
 */
public class Result {

    List<Request> requests;

    List<Item> items;

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Result addItem(Item item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        this.items.add(item);
        return this;
    }
    public Result addItems(List<Item> items) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.addAll(items);
        return this;
    }

    public Result addRequest(Request request) {
        if (requests == null) {
            requests = new ArrayList<>();
        }
        this.requests.add(request);
        return this;
    }
}
