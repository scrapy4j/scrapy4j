package com.victory.scrapy4j.core.component.parser;


import com.victory.scrapy4j.core.component.pojo.Response;
import com.victory.scrapy4j.core.component.pojo.Result;

public interface Parser {
    Result parse(Response response);
}