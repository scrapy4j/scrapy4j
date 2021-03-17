package com.victory.scrapy4j.core.component.pojo;

import cn.hutool.core.lang.Assert;
import com.victory.scrapy4j.core.component.downloader.middleware.FeignDownloaderMiddleware;
import com.victory.scrapy4j.core.component.parser.Parser;
import com.victory.scrapy4j.core.component.resolver.MapResolver;
import com.victory.scrapy4j.core.component.spider.IRequestBody;
import com.victory.scrapy4j.core.component.spider.Spider;
import com.victory.scrapy4j.core.support.feign.FeignSettings;
import com.victory.scrapy4j.core.utils.Utils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class Request implements Serializable {
    private Spider spider;
    private Settings settings;
    private Parser parser;
    private String url;
    private Map<String, Object> headers;
    private Map<String, Object> queries;
    private feign.Request.HttpMethod httpMethod;
    private IRequestBody requestBody;
    private Map<String, Object> variables;
    //经过处理后的variables
    private Map<String, Object> resolvedVariables;
    private MapResolver headersResolver;
    private MapResolver queriesResolver;
    private MapResolver variablesResolver;

    public Request() {
    }

    protected Request(RequestBuilder builder) {
        this.spider = builder.spider;
        this.queries = builder.queries;
        this.settings = builder.settings;
        this.requestBody = builder.requestBody;
        this.url = builder.url;
        this.variables = builder.variables;
        this.parser = builder.parser;
        this.httpMethod = builder.httpMethod;
        this.headers = builder.headers;
        this.headersResolver = builder.headersResolver;
        this.queriesResolver = builder.queriesResolver;
        this.variablesResolver = builder.variablesResolver;
    }

    public static RequestBuilder builder(String url, feign.Request.HttpMethod httpMethod) {
        return new RequestBuilder(url, httpMethod);
    }

    public static RequestBuilder builder(String url) {
        return new RequestBuilder(url, feign.Request.HttpMethod.GET);
    }

    public RequestBuilder toBuilder() {
        return new RequestBuilder(this.url, this.httpMethod)
                .headers(this.headers)
                .headersResolver(this.headersResolver)
                .queries(this.queries)
                .queriesResolver(this.queriesResolver)
                .variables(this.variables)
                .variablesResolver(this.variablesResolver)
                .requestBody(this.requestBody)
                .parser(this.parser)
                .settings(this.settings)
                .spider(this.spider);
    }

    public void setSpider(Spider spider) {
        this.spider = spider;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public Spider getSpider() {
        return spider;
    }

    public Settings getSettings() {
        return settings;
    }

    public Parser getParser() {
        return parser;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, Object> getHeaders() {
        if (headersResolver != null) {
            if (headers == null) {
                headers = new LinkedHashMap<>();
            }
            headers.putAll(headersResolver.resolve(null));
        }
        return headers;
    }

    public Map<String, Object> getQueries() {
        if (queriesResolver != null) {
            if (queries == null) {
                queries = new LinkedHashMap<>();
            }
            queries.putAll(queriesResolver.resolve(null));
        }
        return queries;
    }


    public feign.Request.HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public IRequestBody getRequestBody() {
        return requestBody;
    }

    public Map<String, Object> getVariables() {
        if (variablesResolver != null) {
            if (variables == null) {
                variables = new LinkedHashMap<>();
            }
            variables.putAll(variablesResolver.resolve(null));
        }
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getResolvedVariables() {
        return resolvedVariables;
    }

    public void setResolvedVariables(Map<String, Object> resolvedVariables) {
        this.resolvedVariables = resolvedVariables;
    }

    public Response send() {
        Assert.isNull(settings, "请求配置缺失");
        Response res = new Response();
        try {
            if (this.getSettings() instanceof FeignSettings) {
                res = new FeignDownloaderMiddleware(this, (FeignSettings) this.getSettings()).download();
            } else {
                throw new Exception("FeignSettings required");
            }
        } catch (Exception ex) {
            Utils.logError(this.getSettings().getLogger(this.getClass()), String.format("download error:%s %s", this.getUrl(), ex.getMessage()), ex);
        }
        return res;
    }

    public static class RequestBuilder<T extends RequestBuilder> {
        private String url;
        private feign.Request.HttpMethod httpMethod;
        private Map<String, Object> headers;
        private Map<String, Object> queries;
        private Map<String, Object> variables;
        private MapResolver headersResolver;
        private MapResolver queriesResolver;
        private MapResolver variablesResolver;
        private IRequestBody requestBody;
        private Settings settings;
        private Parser parser;
        private Spider spider;

        public RequestBuilder(String url, feign.Request.HttpMethod httpMethod) {
            this.url = url;
            this.httpMethod = httpMethod;
        }

        public RequestBuilder() {
        }

        public RequestBuilder(String url) {
            this.url = url;
            this.httpMethod = feign.Request.HttpMethod.GET;
        }

        public T httpMethod(feign.Request.HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return (T) this;
        }


        public T spider(Spider spider) {
            this.spider = spider;
            return (T) this;
        }

        public T settings(Settings settings) {
            this.settings = settings;
            return (T) this;
        }

        public T parser(Parser parser) {
            this.parser = parser;
            return (T) this;
        }

        public T headers(Map<String, Object> headers) {
            this.headers = headers;
            return (T) this;
        }

        public T addHeader(String key, Object value) {
            if (this.headers == null) {
                this.headers = new LinkedHashMap<>();
            }
            this.headers.put(key, value);
            return (T) this;
        }

        public T addHeader(String key, Function<Object, String> function) {
            if (this.headers == null) {
                this.headers = new LinkedHashMap<>();
            }
            this.headers.put(key, function);
            return (T) this;
        }

        public T addHeaders(Map<String, Object> headers) {
            if (this.headers == null) {
                this.headers = new LinkedHashMap<>();
            }
            this.headers.putAll(headers);
            return (T) this;
        }

        public T queries(Map<String, Object> queries) {
            this.queries = queries;
            return (T) this;
        }

        public T addQuery(String key, Object value) {
            if (this.queries == null) {
                this.queries = new LinkedHashMap<>();
            }
            this.queries.put(key, value);
            return (T) this;
        }

        public T addQuery(String key, Function<Object, String> function) {
            if (this.queries == null) {
                this.queries = new LinkedHashMap<>();
            }
            this.queries.put(key, function);
            return (T) this;
        }

        public T addQueries(Map<String, Object> queries) {
            if (this.queries == null) {
                this.queries = new LinkedHashMap<>();
            }
            this.queries.putAll(queries);
            return (T) this;
        }

        public T requestBody(IRequestBody requestBody) {
            this.requestBody = requestBody;
            return (T) this;
        }

        public T requestBody(Object requestBody) {
            this.requestBody = () -> requestBody;
            return (T) this;
        }

        public T variables(Map<String, Object> variables) {
            this.variables = variables;
            return (T) this;
        }

        public T addVariable(String key, Object value) {
            if (this.variables == null) {
                this.variables = new LinkedHashMap<>();
            }
            this.variables.put(key, value);
            return (T) this;
        }

        public T addVariables(Map<String, Object> variables) {
            if (this.variables == null) {
                this.variables = new LinkedHashMap<>();
            }
            this.variables.putAll(variables);
            return (T) this;
        }

        public T addVariable(String key, Function<Object, String> function) {
            if (this.variables == null) {
                this.variables = new LinkedHashMap<>();
            }
            this.variables.put(key, function);
            return (T) this;
        }

        public T headersResolver(MapResolver headersResolver) {
            this.headersResolver = headersResolver;
            return (T) this;
        }

        public T queriesResolver(MapResolver queriesResolver) {
            this.queriesResolver = queriesResolver;
            return (T) this;
        }

        public T variablesResolver(MapResolver variablesResolver) {
            this.variablesResolver = variablesResolver;
            return (T) this;
        }

        public T clone(Request request) {
            if (request != null) {
                this.headers(request.getHeaders())
                        .queries(request.getQueries())
                        .variables(request.getVariables())
                        .requestBody(request.getRequestBody())
                        .parser(request.getParser())
                        .settings(request.getSettings())
                        .spider(request.getSpider());
            }
            return (T) this;
        }

        public Request build() {
            return new Request(this);
        }

        public Request combine(Request other) {
            if (other != null) {
                if (this.spider == null) {
                    this.spider(other.getSpider());
                }
                if (this.headers == null) {
                    this.headers(other.getHeaders());
                }
                if (this.queries == null) {
                    this.queries(other.getQueries());
                }
                if (this.requestBody == null) {
                    this.requestBody(other.getRequestBody());
                }
                if (this.parser == null) {
                    this.parser(other.getParser());
                }
                if (this.variables != null && other.getVariables() != null) {
                    Map<String, Object> mergedVariables = other.getVariables();
                    mergedVariables.putAll(this.variables);
                    this.variables(mergedVariables);//merge variables
                }
                if (this.variables == null) {
                    this.variables(other.getVariables());
                }
            }
            return this.build();
        }
    }
}