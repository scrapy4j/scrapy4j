package org.scrapy4j.core.pojo;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import org.scrapy4j.core.selectors.JSONSelector;
import org.scrapy4j.core.selectors.XMLSelector;
import org.springframework.util.MultiValueMap;


public class Response {

    private transient Request request;
    private Object body;
    private int status;
    MultiValueMap<String, String> headers;

    public Response(){
    }

    public Response(Object body,MultiValueMap<String, String> headers, int status){
        this.body=body;
        this.headers=headers;
        this.status=status;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MultiValueMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(MultiValueMap<String, String> headers) {
        this.headers = headers;
    }

    public String toJSONString() {
        String[] excludeProperties = {"request"};
        PropertyPreFilters filters = new PropertyPreFilters();
        PropertyPreFilters.MySimplePropertyPreFilter excludeFilter = filters.addFilter();
        excludeFilter.addExcludes(excludeProperties);
        return JSONObject.toJSONString(this, excludeFilter);
    }

    public JSONSelector json(String path){
        return new JSONSelector(JSONUtil.parse(body),path);
    }

    public XMLSelector xml(String xpath){
        return new XMLSelector(body,xpath);
    }
}
