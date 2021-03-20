package scrapy4j.core.support.mybatis.toolkit;

@FunctionalInterface
public interface BiIntFunction<T, R> {
    R apply(T t, int i);
}
