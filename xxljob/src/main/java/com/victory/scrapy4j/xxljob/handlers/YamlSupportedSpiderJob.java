package com.victory.scrapy4j.xxljob.handlers;

import com.victory.scrapy4j.core.component.Crawler;
import com.victory.scrapy4j.xxljob.support.spring.Configuration;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class YamlSupportedSpiderJob {
    @Autowired
    BeanFactory beanFactory;

    @XxlJob("YamlSpiderJobHandler")
    public ReturnT<String> spiderJobHandler(String param) throws Exception {
        XxlJobLogger.log("spider job begin");
        try {
            Configuration configuration = new Configuration(beanFactory);
            Crawler crawler = configuration.loadCrawler(param);
            crawler.crawl();
        } catch (RuntimeException ex) {
            XxlJobLogger.log(ex);
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }
}
