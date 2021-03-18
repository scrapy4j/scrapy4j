package org.scrapy4j.core.parser;


import org.scrapy4j.core.pojo.Response;
import org.scrapy4j.core.pojo.Result;

public interface Parser {
    Result parse(Response response);
}
