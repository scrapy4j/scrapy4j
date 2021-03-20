package scrapy4j.core.parser;


import scrapy4j.core.pojo.Response;
import scrapy4j.core.pojo.Result;

public interface Parser {
    Result parse(Response response);
}
