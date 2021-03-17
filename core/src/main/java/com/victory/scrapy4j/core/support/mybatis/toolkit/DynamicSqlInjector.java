package com.victory.scrapy4j.core.support.mybatis.toolkit;

import com.victory.scrapy4j.core.support.mybatis.core.injector.ISqlInjector;
import com.victory.scrapy4j.core.support.mybatis.core.metadata.TableInfo;
import com.victory.scrapy4j.core.support.mybatis.method.AbstractMethod;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/*
copied from mybatis-plus
*/
public class DynamicSqlInjector implements ISqlInjector {

    private List<AbstractMethod> methods;

    public DynamicSqlInjector(AbstractMethod... methods) {
        this.methods = Arrays.asList(methods);
    }

    public void inspectInject(Configuration configuration, TableInfo tableInfo) {
        String mapperName = AbstractMethod.getMapperName(tableInfo.getTableName());
        String resource = mapperName.replace('.', '/') + ".java (best guess)";
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, resource);
        builderAssistant.setCurrentNamespace(mapperName);
        Class<?> modelClass = Object.class;
        Set<String> mapperRegistryCache = GlobalConfigUtils.getMapperRegistryCache();
        if (!mapperRegistryCache.contains(mapperName)) {
            List<AbstractMethod> methodList = this.getMethodList();
            if (CollectionUtils.isNotEmpty(methodList)) {
                methodList.forEach(m -> m.inject(builderAssistant, mapperName, modelClass, tableInfo));
            } else {
            }
            mapperRegistryCache.add(mapperName);
        }
    }

    /**
     * <p>
     * 获取 注入的方法
     * </p》
     *
     * @return 注入的方法集合
     * @since 3.1.2 add  mapperClass
     */
    public List<AbstractMethod> getMethodList() {
        return methods;
    }

    @Override
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {

    }
}
