package org.scrapy4j.core.support.mybatis.method;


import org.scrapy4j.core.support.mybatis.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;

public class SaveOrUpdate extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(String mapperName, Class<?> modelClass, TableInfo tableInfo) {
        //        if (null != entity) {
//            Class<?> cls = entity.getClass();
//            TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
//            Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!");
//            String keyProperty = tableInfo.getKeyProperty();
//            Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!");
//            Object idVal = ReflectionKit.getFieldValue(entity, tableInfo.getKeyProperty());
//            return StringUtils.checkValNull(idVal) || Objects.isNull(getById((Serializable) idVal)) ? save(entity) : updateById(entity);
//        }
        return null;
    }
}
