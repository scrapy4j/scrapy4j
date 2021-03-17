package com.victory.scrapy4j.core.support.mybatis.core.injector;

import com.victory.scrapy4j.core.support.mybatis.method.*;
import com.victory.scrapy4j.core.support.mybatis.method.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultSqlInjector extends AbstractSqlInjector {
    public DefaultSqlInjector() {
    }

    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        return (List) Stream.of(new Insert(),  new Update(), new UpdateById(), new SelectById(),  new SaveOrUpdate(), new ReplaceInto()).collect(Collectors.toList());
    }

    @Override
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {

    }
}