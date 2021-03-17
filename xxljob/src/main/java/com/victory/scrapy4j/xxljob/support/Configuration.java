package com.victory.scrapy4j.xxljob.support;

import com.victory.scrapy4j.core.component.Crawler;
import com.victory.scrapy4j.core.component.itempipeline.RDBItemPipeline;
import com.victory.scrapy4j.core.component.itempipeline.RedisItemPipeline;
import com.victory.scrapy4j.core.component.parser.Parser;
import com.victory.scrapy4j.core.component.pojo.Request;
import com.victory.scrapy4j.core.component.pojo.StartSql;
import com.victory.scrapy4j.core.component.resolver.MapResolver;
import com.victory.scrapy4j.core.component.spider.Spider;
import com.victory.scrapy4j.core.component.spider.SqlStartRequests;
import com.victory.scrapy4j.core.component.spider.StartRequests;
import com.victory.scrapy4j.core.support.feign.FeignSettings;
import com.victory.scrapy4j.core.support.mybatis.core.enums.FieldStrategy;
import com.victory.scrapy4j.core.support.mybatis.core.enums.IdType;
import com.victory.scrapy4j.core.support.redis.toolkit.RedisService;
import com.victory.scrapy4j.xxljob.support.parser.JSONProperty2ItemParser;
import com.victory.scrapy4j.xxljob.support.parser.JSONPropertyRDBItemParser;
import com.victory.scrapy4j.xxljob.support.parser.itemdefinition.RDBItemDefinition;
import com.victory.scrapy4j.xxljob.support.parser.itemdefinition.RedisItemDefinition;
import com.victory.scrapy4j.xxljob.support.definition.CrawlerDefinition;
import com.victory.scrapy4j.xxljob.support.parser.JSONPropertyMapper;
import com.victory.scrapy4j.xxljob.support.parser.transformstrategy.TransformStrategy;
import com.victory.scrapy4j.xxljob.support.resolver.argument.*;
import com.victory.scrapy4j.xxljob.utils.XxlJobLogHelper;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.ibatis.session.SqlSessionFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;


public class Configuration {

    protected Registry registry;

    MapArgumentResolver spiderArgumentResolver;

    public static final String CONSTRUCTOR = "Constructor";
    public static final String BUILD = "build";

    public Configuration() {
        this.registry = new Registry();

        //requests
        MapArgumentResolver requestArgumentResolver = new MapArgumentResolver(Request.RequestBuilder.class)
                .argumentResolver("headersResolver", new SpELArgumentResolver())//这里指定只能使用spel，不接受其他类型例如字符串
                .argumentResolver("queriesResolver", new SpELArgumentResolver())
                .argumentResolver("variablesResolver", new SpELArgumentResolver())
                .argumentResolver("headers", new MapArgumentResolver(Map.class))//这里指定MapArgumentResolver可以进行SpEL的处理，不指定的话将不会解析SpEL
                .argumentResolver("queries", new MapArgumentResolver(Map.class))
                .argumentResolver("variables", new MapArgumentResolver(Map.class))
                .argumentResolver("body", new BodyArgumentResolver(Map.class))
                .methodInvoke("httpMethod", new String[]{"method"}, new Class[]{feign.Request.HttpMethod.class})
                .methodInvoke("headers", new String[]{"headers"}, new Class[]{Map.class})
                .methodInvoke("queries", new String[]{"queries"}, new Class[]{Map.class})
                .methodInvoke("variables", new String[]{"variables"}, new Class[]{Map.class})
                .methodInvoke("headersResolver", new String[]{"headersResolver"}, new Class[]{MapResolver.class})
                .methodInvoke("queriesResolver", new String[]{"queriesResolver"}, new Class[]{MapResolver.class})
                .methodInvoke("variablesResolver", new String[]{"variablesResolver"}, new Class[]{MapResolver.class})
                .methodInvoke("requestBody", new String[]{"body"}, new Class[]{Object.class});

        //propertyMappings
        ListArgumentResolver propertyMappingArgumentResolver = new ListArgumentResolver()
                .argumentResolver(new JSONPropertyMapperArgumentResolver(JSONPropertyMapper.JSONPropertyMapperBuilder.class)
                        .argumentResolver("transformStrategy", new TransformStrategyNameArgsArgumentResolver())
                        .methodInvoke("primaryKey", new String[]{"primaryKey"}, new Class[]{Boolean.class})
                        .methodInvoke("idType", new String[]{"idType"}, new Class[]{IdType.class})
                        .methodInvoke("insertStrategy", new String[]{"insertStrategy"}, new Class[]{FieldStrategy.class})
                        .methodInvoke("updateStrategy", new String[]{"updateStrategy"}, new Class[]{FieldStrategy.class})
                        .methodInvoke("propertyValue", new String[]{"propertyValue"}, new Class[]{Object.class})
                        .methodInvoke("transformStrategy", new String[]{"transformStrategy"}, new Class[]{TransformStrategy.class})
                        .methodInvoke(CONSTRUCTOR, new String[]{"propertyExpression", "columnName"}, new Class[]{String.class, String.class})
                        .methodInvoke(BUILD, null, null));

        spiderArgumentResolver = new MapArgumentResolver(Spider.SpiderBuilder.class)
                .argumentResolver("startRequests", new NameArgsMapArgumentResolver()
                        .argumentResolver("simple", new MapArgumentResolver(StartRequests.class)
                                .argumentResolver("requests", new ListArgumentResolver()
                                        .argumentResolver(requestArgumentResolver.clone()
                                                .methodInvoke(CONSTRUCTOR, new String[]{"url"}, new Class[]{String.class})
                                                .methodInvoke(BUILD, null, null))
                                )
                                .methodInvoke("addRequests", new String[]{"requests"}, new Class[]{List.class})
                        )
                        .argumentResolver("sql", new MapArgumentResolver(SqlStartRequests.SqlStartRequestsBuilder.class)
                                .argumentResolver("startSql", new MapArgumentResolver(StartSql.StartSqlBuilder.class)
                                        .methodInvoke("sql",new String[]{"sql"},new Class<?>[]{String.class})
                                        .methodInvoke("pageSize",new String[]{"pageSize"},new Class<?>[]{Integer.class})
                                        .methodInvoke("sqlParameters",new String[]{"sqlParameters"},new Class<?>[]{Map.class})
                                        .methodInvoke(BUILD,null,null))
                                .argumentResolvers(requestArgumentResolver.getDelegates())
                                .argumentResolver("sqlSessionFactory", new SpELArgumentResolver())
                                .methodInvokeList(requestArgumentResolver.getMethodInvokeList())
                                .propertySetList(requestArgumentResolver.getPropertySetList())
                                .methodInvoke(CONSTRUCTOR, new String[]{"url", "startSql", "sqlSessionFactory", "method"}, new Class<?>[]{String.class, StartSql.class, SqlSessionFactory.class, feign.Request.HttpMethod.class})
                                .methodInvoke(BUILD, null, null)
                        )
                )
                .methodInvoke("startRequests", new String[]{"startRequests"}, new Class[]{StartRequests.class});

        spiderArgumentResolver.argumentResolver("itemPipelines", new ListArgumentResolver()
                .argumentResolver(new NameArgsMapArgumentResolver()
                        .argumentResolver("rdb", new MapArgumentResolver(RDBItemPipeline.class)
                                .argumentResolver("sqlSessionFactory", new SpELArgumentResolver())
                                .methodInvoke(CONSTRUCTOR, new String[]{"sqlSessionFactory"}, new Class<?>[]{SqlSessionFactory.class}))
                        .argumentResolver("redis", new MapArgumentResolver(RedisItemPipeline.class)
                                .argumentResolver("redisService", new SpELArgumentResolver())
                                .methodInvoke(CONSTRUCTOR, new String[]{"redisService"}, new Class<?>[]{RedisService.class}))
                )
        )
                .methodInvoke("itemPipelines", new String[]{"itemPipelines"}, new Class[]{List.class});

        spiderArgumentResolver.argumentResolver("parser", new NameArgsMapArgumentResolver()
                .argumentResolver("json2item", new MapArgumentResolver(JSONProperty2ItemParser.JSONProperty2ItemParserBuilder.class)
                        .argumentResolver("interceptors", new ListArgumentResolver()
                                .argumentResolver(new NameArgsMapArgumentResolver()))
                        .argumentResolver("items", new ListArgumentResolver()
                                .argumentResolver(new NameArgsMapArgumentResolver()
                                        .argumentResolver("rdb", new MapArgumentResolver(RDBItemDefinition.class)
                                                .argumentResolver("propertyMappings", propertyMappingArgumentResolver)
                                                .methodInvoke(CONSTRUCTOR, new String[]{"propertyMappings", "tableName"}, new Class[]{List.class, String.class}))
                                        .argumentResolver("redis", new MapArgumentResolver(RedisItemDefinition.class))))
                        .methodInvoke(CONSTRUCTOR, new String[]{"items", "interceptors"}, new Class<?>[]{List.class, List.class})
                        .methodInvoke(BUILD, null, null))
                .argumentResolver("json2rdb", new MapArgumentResolver(JSONPropertyRDBItemParser.JSONPropertyRDBItemParserBuilder.class)
                        .argumentResolver("interceptors", new ListArgumentResolver()
                                .argumentResolver(new NameArgsMapArgumentResolver()))
                        .argumentResolver("propertyMappings", propertyMappingArgumentResolver)
                        .methodInvoke(CONSTRUCTOR, new String[]{"propertyMappings", "tableName"}, new Class<?>[]{List.class, String.class})
                        .methodInvoke("interceptors", new String[]{"interceptors"}, new Class<?>[]{List.class})
                        .methodInvoke(BUILD, null, null)))
                .methodInvoke("parser", new String[]{"parser"}, new Class[]{Parser.class})
                .methodInvoke(BUILD, null, null);
    }

    public Crawler loadCrawler(String yaml) {
        CrawlerDefinition crawlerDefinition = new Yaml().loadAs(yaml, CrawlerDefinition.class);

        Spider spider = (Spider) spiderArgumentResolver.resolve(registry, crawlerDefinition.getSpider());

        // generate spider-setting
        FeignSettings settings = FeignSettings.builder(crawlerDefinition.getSettings())
                .logger(new XxlJobLogHelper().getProxy(new XxlJobLogger()))
                .build();

        return Crawler.builder()
                .spider(spider)
                .settings(settings)
                .build();
    }

    public Registry getRegistry() {
        return registry;
    }
}
