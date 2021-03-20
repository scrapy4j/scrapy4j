package scrapy4j.core.item;

public interface Item<T> {
    T values();
    void setValues(T t);
}