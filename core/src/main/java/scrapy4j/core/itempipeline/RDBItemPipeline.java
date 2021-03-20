package scrapy4j.core.itempipeline;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import scrapy4j.core.item.Item;
import scrapy4j.core.item.RDBItem;
import scrapy4j.core.spider.Spider;
import scrapy4j.core.support.mybatis.core.enums.SqlMethod;
import scrapy4j.core.support.mybatis.method.*;
import scrapy4j.core.support.mybatis.toolkit.DynamicSqlInjector;
import scrapy4j.core.utils.Utils;

import java.util.List;

public class RDBItemPipeline implements ItemPipeline<Object> {
    private SqlSessionTemplate sqlSessionTemplate;

    public RDBItemPipeline(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public RDBItemPipeline(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
    }

    @Override
    public void processItem(List<Item<Object>> items, Spider spider) {
        items.forEach(m -> {
            if (this.support(m)) {
                try {
                    SqlMethod sqlMethod = ((RDBItem) m).getSqlMethod();
                    if (sqlMethod == SqlMethod.SAVE_OR_UPDATE) {
                        saveOrUpdate((RDBItem) m);
                    } else if (sqlMethod == SqlMethod.INSERT_ONE) {
                        new DynamicSqlInjector(new Insert()).inspectInject(sqlSessionTemplate.getConfiguration(), ((RDBItem) m));
                        sqlSessionTemplate.insert(
                                AbstractMethod.getStatementName(
                                        ((RDBItem) m).getTableName(),
                                        SqlMethod.INSERT_ONE.getMethod()
                                ),
                                m.values()
                        );
                    } else if (sqlMethod == SqlMethod.UPDATE_BY_ID) {
                        new DynamicSqlInjector(new UpdateById()).inspectInject(sqlSessionTemplate.getConfiguration(), ((RDBItem) m));
                        sqlSessionTemplate.update(
                                AbstractMethod.getStatementName(
                                        ((RDBItem) m).getTableName(),
                                        SqlMethod.UPDATE_BY_ID.getMethod()
                                ),
                                m.values()
                        );
                    } else {
                        //  default replaceInto?
                        new DynamicSqlInjector(new ReplaceInto()).inspectInject(sqlSessionTemplate.getConfiguration(), ((RDBItem) m));
                        sqlSessionTemplate.update(
                                AbstractMethod.getStatementName(
                                        ((RDBItem) m).getTableName(),
                                        SqlMethod.REPLACE_INTO.getMethod()
                                ),
                                m.values()
                        );
                    }
                } catch (Exception ex) {
                    Utils.logError(spider.getSettings().getLogger(this.getClass()), String.format("pipeline error:%s", ex.getMessage()), ex);
                }
            }
        });
    }

    /**
     * 保存或者修改
     *
     * @param m
     */
    private void saveOrUpdate(RDBItem m) {
        String tableName = m.getTableName();
        new DynamicSqlInjector(new SelectById(), new Insert(), new UpdateById()).inspectInject(sqlSessionTemplate.getConfiguration(), m);

        // 根据id获取实体对象
        Object entity = sqlSessionTemplate.selectOne(AbstractMethod.getStatementName(
                tableName,
                SqlMethod.SELECT_BY_ID.getMethod()
                ),
                m.values());
        if (entity == null) {
            // 实体对象为空，新增
            sqlSessionTemplate.insert(AbstractMethod.getStatementName(
                    tableName,
                    SqlMethod.INSERT_ONE.getMethod()
                    ),
                    m.values());
        } else {
            // 实体对象不为空，修改
            sqlSessionTemplate.update(AbstractMethod.getStatementName(
                    tableName,
                    SqlMethod.UPDATE_BY_ID.getMethod()
                    ),
                    m.values());
        }
    }

    @Override
    public boolean support(Item item) {
        return item instanceof RDBItem;
    }
}
