package scrapy4j.core.support.feign;


import scrapy4j.core.pojo.Request;
import scrapy4j.core.utils.Utils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Util;
import feign.template.UriTemplate;

import java.util.Map;

public class RequestTemplateResolveInterceptor implements RequestInterceptor {

    Request request;

    public RequestTemplateResolveInterceptor(Request request) {
        this.request = request;
    }

    @Override
    public void apply(RequestTemplate template) {
        UriTemplate uriTemplate = UriTemplate.create(this.request.getUrl(), false, Util.UTF_8);

        RequestTemplate templateToResolve = new RequestTemplate();

        // header
        Map<String, Object> headers = this.request.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            // 数据处理
            Map<String, Object> targetMap = Utils.mapResolve(headers);
            for (String key : targetMap.keySet()) {
                templateToResolve.header(key, String.valueOf(targetMap.get(key)));
            }
        }

        // queries
        Map<String, Object> queries = this.request.getQueries();
        if (queries != null && !queries.isEmpty()) {
            // 数据处理
            Map<String, Object> targetMap = Utils.mapResolve(queries);
            for (String key : targetMap.keySet()) {
                templateToResolve.query(key, String.valueOf(targetMap.get(key)));
            }
        }

        String uri;

        // variables
        Map<String, Object> variables = this.request.getVariables();
        if (variables != null) {
            Map<String, Object> targetMap = this.request.getResolvedVariables();//有可能在FeignMiddleware body处理时已经赋值
            if (targetMap == null) {
                targetMap = Utils.mapResolve(variables);
                this.request.setResolvedVariables(targetMap);//将解析后的variables进行存储
            }
            // 数据处理
            uri = uriTemplate.expand(targetMap);
            templateToResolve = templateToResolve.resolve(targetMap);
        } else {
            uri = uriTemplate.toString();
        }

        template.headers(templateToResolve.headers());
        template.queries(templateToResolve.queries());

        template.target(uri);
        if (this.request.getUrl().endsWith("/")) {
            template.uri(null);//add slash
        }
//        this.request.setUrl(template.url());//for log beauty
    }
}
