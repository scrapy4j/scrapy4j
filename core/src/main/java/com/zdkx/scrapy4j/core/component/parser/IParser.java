package com.zdkx.scrapy4j.core.component.parser;


import com.zdkx.scrapy4j.core.component.pojo.Response;
import com.zdkx.scrapy4j.core.component.pojo.Result;

public interface IParser {
    Result parse(Response response);
}
