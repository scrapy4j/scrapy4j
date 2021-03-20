package scrapy4j.xxljob.spring;

import org.springframework.beans.factory.BeanFactory;

public class Configuration extends scrapy4j.xxljob.Configuration {

    public Configuration(BeanFactory beanFactory) {
        super();
        super.registry = new Registry(beanFactory);
    }
}
