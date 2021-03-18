package org.scrapy4j.xxljob;

import org.scrapy4j.core.itempipeline.RDBItemPipeline;
import org.scrapy4j.core.itempipeline.RedisItemPipeline;
import org.scrapy4j.core.spider.SqlStartRequests;
import org.scrapy4j.core.spider.StartRequests;
import org.scrapy4j.xxljob.parser.JSONProperty2ItemParser;
import org.scrapy4j.xxljob.parser.JSONPropertyRDBItemParser;
import org.scrapy4j.xxljob.parser.itemdefinition.ItemDefinition;
import org.scrapy4j.xxljob.parser.itemdefinition.RDBItemDefinition;
import org.scrapy4j.xxljob.parser.itemdefinition.RedisItemDefinition;
import org.scrapy4j.xxljob.parser.transformstrategy.DefaultValueTransformStrategy;
import org.scrapy4j.xxljob.parser.transformstrategy.JSON2StringTransformStrategy;
import org.scrapy4j.xxljob.parser.transformstrategy.NoneTransformStrategy;
import org.scrapy4j.xxljob.parser.transformstrategy.SqlTransformStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Registry {

    public interface Requests {
        String SIMPLE_START_REQUESTS = "simple";
        String SQL_START_REQUESTS = "sql";
    }

    public interface ItemPipeline {
        String RDB_ITEM_PIPELINE = "rdb";
        String REDIS_ITEM_PIPELINE = "redis";
    }

    public interface Parser {
        String JSON_2_RDB_PARSER = "json2rdb";
        String JSON_2_ITEM_PARSER = "json2item";
    }

    public interface Item {
        String RDB_ITEM = "rdb";
        String REDIS_ITEM = "redis";
    }

    public interface TransformStrategy {
        String DEFAULT_VALUE_TRANSFORM_STRATEGY = "defaultValue";
        String JSON_2_STRING_TRANSFORM_STRATEGY = "json2string";
        String NONE_TRANSFORM_STRATEGY = "none";
        String SQL_TRANSFORM_STRATEGY = "sql";
    }

    private final Map<String, Class<? extends StartRequests>> startRequestsMap = new HashMap<>();

    private final Map<String, Class<? extends org.scrapy4j.core.itempipeline.ItemPipeline>> itemPipelineMap = new HashMap<>();

    private final Map<String, Class<? extends org.scrapy4j.core.parser.Parser>> parserMap = new HashMap<>();

    private final Map<String, Class<? extends ItemDefinition>> itemDefinitionMap = new HashMap<>();

    private final Map<String, Class<? extends org.scrapy4j.xxljob.parser.transformstrategy.TransformStrategy>> transformStrategyMap = new HashMap<>();

    protected final Map<String, Object> sharedObjectMap = new HashMap<>();

//    private final Map<String, Function<Object, String>> functionMap = new ConcurrentHashMap<>();

//    private final Map<String, BiConsumer<Response, Result>> pageCallbackMap = new ConcurrentHashMap<>();
//
//    private final Map<String, Predicate<Response>> parserPredicateMap = new ConcurrentHashMap<>();

    public Registry() {
        registerStartRequests(Requests.SIMPLE_START_REQUESTS, StartRequests.class);
        registerStartRequests(Requests.SQL_START_REQUESTS, SqlStartRequests.class);

        registerItemPipeline(ItemPipeline.RDB_ITEM_PIPELINE, RDBItemPipeline.class);
        registerItemPipeline(ItemPipeline.REDIS_ITEM_PIPELINE, RedisItemPipeline.class);

        registerParser(Registry.Parser.JSON_2_RDB_PARSER, JSONPropertyRDBItemParser.class);
        registerParser(Registry.Parser.JSON_2_ITEM_PARSER, JSONProperty2ItemParser.class);

        registerItemDefinition(Item.RDB_ITEM, RDBItemDefinition.class);
        registerItemDefinition(Item.REDIS_ITEM, RedisItemDefinition.class);

        registerTransformStrategy(TransformStrategy.DEFAULT_VALUE_TRANSFORM_STRATEGY, DefaultValueTransformStrategy.class);
        registerTransformStrategy(TransformStrategy.JSON_2_STRING_TRANSFORM_STRATEGY, JSON2StringTransformStrategy.class);
        registerTransformStrategy(TransformStrategy.NONE_TRANSFORM_STRATEGY, NoneTransformStrategy.class);
        registerTransformStrategy(TransformStrategy.SQL_TRANSFORM_STRATEGY, SqlTransformStrategy.class);
    }

    public void registerStartRequests(String alias, Class<? extends StartRequests> startRequests) {
        startRequestsMap.put(alias, startRequests);
    }

    public Class<? extends StartRequests> getStartRequests(String alias) {
        return startRequestsMap.get(alias);
    }

    public void registerItemPipeline(String alias, Class<? extends org.scrapy4j.core.itempipeline.ItemPipeline> itemPipeline) {
        itemPipelineMap.put(alias, itemPipeline);
    }

    public Class<? extends org.scrapy4j.core.itempipeline.ItemPipeline> getItemPipeline(String alias) {
        return itemPipelineMap.get(alias);
    }

    public void registerParser(String alias, Class<? extends org.scrapy4j.core.parser.Parser> parser) {
        parserMap.put(alias, parser);
    }

    public Class<? extends org.scrapy4j.core.parser.Parser> getParser(String alias) {
        return parserMap.get(alias);
    }

    public void registerItemDefinition(String alias, Class<? extends ItemDefinition> itemDefinition) {
        itemDefinitionMap.put(alias, itemDefinition);
    }

    public Class<? extends ItemDefinition> getItemDefinition(String alias) {
        return itemDefinitionMap.get(alias);
    }

    public void registerTransformStrategy(String alias, Class<? extends org.scrapy4j.xxljob.parser.transformstrategy.TransformStrategy> transformStrategy) {
        transformStrategyMap.put(alias, transformStrategy);
    }

    public Class<? extends org.scrapy4j.xxljob.parser.transformstrategy.TransformStrategy> getTransformStrategy(String alias) {
        return transformStrategyMap.get(alias);
    }

    public void registerSharedObject(String alias, Object sharedObject) {
//        if (sharedObjectMap.get(alias) != null) {
//            Object existVal = sharedObjectMap.get(alias);
//            if (existVal instanceof InnerList) {//继续使用inner list
//                InnerList<Object> existList = (InnerList<Object>) existVal;
//                existList.add(sharedObject);
//                sharedObject = existList;
//            } else {//多条记录时初始化inner list，为了解决spring bean是多例的情况，取值时对应多例
//                InnerList<Object> valList = new InnerList<>();
//                valList.add(existVal);
//                valList.add(sharedObject);
//                sharedObject = valList;
//            }
//        }
        sharedObjectMap.put(alias, sharedObject);
    }

    public Object getSharedObject(String alias) {
//        Object res = null;
//        Object existVal = sharedObjectMap.get(alias);
//        if (existVal instanceof InnerList) {
//            InnerList<Object> existList = (InnerList<Object>) existVal;
//            if (existList.size() > 0) {
//                res = existList.get(0);
//                existList.remove(0);
//            }
//        } else {
//            res = sharedObjectMap.get(alias);
//        }
        return sharedObjectMap.get(alias);
    }

//    public void registerSharedFunction(String alias, Function<Object, String> function) {
//        functionMap.put(alias, function);
//    }

//    public void registerSharedPageCallback(String alias, BiConsumer<Response, Result> pageCallback) {
//        pageCallbackMap.put(alias, pageCallback);
//    }
//
//    public void registerSharedParserPredicate(String alias, Predicate<Response> parserPredicate) {
//        parserPredicateMap.put(alias, parserPredicate);
//    }

//    public Function<Object, String> getFunction(String key) {
//        return functionMap.get(key);
//    }

//    public BiConsumer<Response, Result> getPageCalllback(String key) {
//        return pageCallbackMap.get(key);
//    }
//
//    public Predicate<Response> getParserPredicate(String key) {
//        return parserPredicateMap.get(key);
//    }

    private class InnerList<T> extends ArrayList<T> {

    }
}
