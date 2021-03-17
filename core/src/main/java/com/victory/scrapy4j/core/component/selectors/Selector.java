package com.victory.scrapy4j.core.component.selectors;

import java.util.List;

public interface Selector {

    /**
     * Serialize and return the matched nodes in a single object.
     */
    public Object get();

    /**
     * Serialize and return the matched node in a 1-element list of objects.
     */
    public List<Object> getAll();
}
