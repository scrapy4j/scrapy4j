package scrapy4j.core.selectors;

import java.util.List;

public interface Selector {

    /**
     * Serialize and return the matched nodes in a single object.
     * @return
     */
    public Object get();

    /**
     * Serialize and return the matched node in a 1-element list of objects.
     * @return
     */
    public List<Object> getAll();
}
