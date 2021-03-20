package scrapy4j.xxljob.argument;
import scrapy4j.xxljob.Registry;

public interface ArgumentResolver <T,R>{
    R resolve(Registry registry, T obj);
}
