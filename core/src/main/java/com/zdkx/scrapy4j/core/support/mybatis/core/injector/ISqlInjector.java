package com.zdkx.scrapy4j.core.support.mybatis.core.injector;

import org.apache.ibatis.builder.MapperBuilderAssistant;

public interface ISqlInjector {
    void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass);
}
