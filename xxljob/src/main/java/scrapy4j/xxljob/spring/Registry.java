package scrapy4j.xxljob.spring;


import scrapy4j.core.utils.Utils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registry extends scrapy4j.xxljob.Registry {

    BeanFactory beanFactory;

    public Registry(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object getSharedObject(String alias) {
        Object obj;
        if (sharedObjectMap.get(alias) != null) {
            obj = sharedObjectMap.get(alias);
        } else {
            Matcher matcher = Pattern.compile(Utils.SPEL_REGX).matcher(alias);
            if (matcher.find()) {
                ExpressionParser elParser = new SpelExpressionParser();
                String entryValue = matcher.group();
                StandardEvaluationContext context = new StandardEvaluationContext();
                context.setBeanResolver(new BeanFactoryResolver(beanFactory));
                Expression expression = elParser.parseExpression(entryValue, new TemplateParserContext());

                obj = expression.getValue(context);
                if (obj == null) {
                    throw new RuntimeException(String.format("cannot find bean:%s", alias));
                }
                // TODO beanName 取值逻辑待优化
                String beanName = expression.getExpressionString().replace("@", "");
                boolean singleton = beanFactory.isSingleton(beanName);
                if (singleton) {
                    this.registerSharedObject(entryValue, obj);
                }

            } else {
                throw new RuntimeException(String.format("cannot find bean:%s", alias));
            }
        }
        return obj;
    }
}
