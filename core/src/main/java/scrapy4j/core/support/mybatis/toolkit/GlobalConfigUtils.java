package scrapy4j.core.support.mybatis.toolkit;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class GlobalConfigUtils {

    private static Set<String> mapperRegistryCache = new ConcurrentSkipListSet();

    public GlobalConfigUtils() {
    }

    public static Set<String> getMapperRegistryCache() {
        return mapperRegistryCache;
    }

}