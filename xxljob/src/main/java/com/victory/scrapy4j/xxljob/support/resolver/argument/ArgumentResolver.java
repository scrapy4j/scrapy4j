package com.victory.scrapy4j.xxljob.support.resolver.argument;
import com.victory.scrapy4j.xxljob.support.Registry;

public interface ArgumentResolver <T,R>{
    R resolve(Registry registry, T obj);
}
