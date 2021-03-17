package com.victory.scrapy4j.xxljob.support.resolver;


import com.victory.scrapy4j.core.component.pojo.Response;
import com.victory.scrapy4j.core.component.pojo.Result;

import java.util.function.BiConsumer;

@Deprecated
public interface IPageCallbackResolver extends IResolver<BiConsumer<Response, Result>> {
}
