package com.victory.scrapy4j.xxljob.support.spring;

import org.springframework.beans.factory.BeanFactory;

public class Configuration extends com.victory.scrapy4j.xxljob.support.Configuration {

    public Configuration(BeanFactory beanFactory) {
        super();
        super.registry = new com.victory.scrapy4j.xxljob.support.spring.Registry(beanFactory);
    }
}
