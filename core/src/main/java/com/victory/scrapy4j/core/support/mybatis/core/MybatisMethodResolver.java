package com.victory.scrapy4j.core.support.mybatis.core;

import java.lang.reflect.Method;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.builder.annotation.MethodResolver;

public class MybatisMethodResolver extends MethodResolver {
    private final MybatisMapperAnnotationBuilder annotationBuilder;
    private final Method method;

    public MybatisMethodResolver(MapperAnnotationBuilder annotationBuilder, Method method) {
        super(annotationBuilder, method);
        this.annotationBuilder = (MybatisMapperAnnotationBuilder)annotationBuilder;
        this.method = method;
    }

    public void resolve() {
        this.annotationBuilder.parseStatement(this.method);
    }
}