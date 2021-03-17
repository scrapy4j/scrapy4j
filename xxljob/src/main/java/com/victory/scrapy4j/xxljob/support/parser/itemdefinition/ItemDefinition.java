package com.victory.scrapy4j.xxljob.support.parser.itemdefinition;


import com.victory.scrapy4j.core.component.item.Item;
import com.victory.scrapy4j.core.component.pojo.Response;

import java.util.List;

public interface ItemDefinition {
    List<Item> extractAndTransformItems(Response response);
}
