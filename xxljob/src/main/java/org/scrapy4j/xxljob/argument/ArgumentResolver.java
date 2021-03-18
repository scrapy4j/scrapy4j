package org.scrapy4j.xxljob.argument;
import org.scrapy4j.xxljob.Registry;

public interface ArgumentResolver <T,R>{
    R resolve(Registry registry, T obj);
}
