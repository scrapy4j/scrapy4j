package org.scrapy4j.xxljob.parser.itemdefinition;


import org.scrapy4j.core.item.Item;
import org.scrapy4j.core.pojo.Response;

import java.util.List;

public interface ItemDefinition {
    List<Item> extractAndTransformItems(Response response);
}
