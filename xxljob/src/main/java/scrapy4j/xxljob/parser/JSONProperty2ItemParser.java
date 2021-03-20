package scrapy4j.xxljob.parser;

import scrapy4j.core.parser.Parser;
import scrapy4j.core.pojo.Response;
import scrapy4j.core.pojo.Result;
import scrapy4j.core.utils.Utils;
import scrapy4j.xxljob.parser.interceptor.Interceptor;
import scrapy4j.xxljob.parser.interceptor.InterceptorChain;
import scrapy4j.xxljob.parser.itemdefinition.ItemDefinition;

import java.util.List;

public class JSONProperty2ItemParser implements Parser {

    List<ItemDefinition> itemDefinitions;

    private InterceptorChain interceptorChain = new InterceptorChain();

    public static JSONProperty2ItemParserBuilder builder(List<ItemDefinition> itemDefinitions) {
        return new JSONProperty2ItemParserBuilder(itemDefinitions);
    }

    private JSONProperty2ItemParser(JSONProperty2ItemParserBuilder builder) {
        this.itemDefinitions = builder.itemDefinitions;
        this.interceptorChain = builder.interceptorChain;
    }

    @Override
    public Result parse(Response response) {
        Result result = new Result();

        //pre handle
        if (!interceptorChain.applyPreHandle(response.getRequest(), response, result)) {
            return result;
        }

        //extract&transform
        try {
            itemDefinitions.forEach(itemDefinition -> {
                result.addItems(itemDefinition.extractAndTransformItems(response));
            });

            //post handle
            interceptorChain.applyPostHandle(response.getRequest(), response, result);

        } catch (Exception ex) {
            Utils.logError(response.getRequest().getSettings().getLogger(this.getClass()),"JSONProperty2ItemParser parse error",ex );
            interceptorChain.triggerAfterCompletion(response.getRequest(), response, result, ex);
        }

        return result;
    }

    public List<ItemDefinition> getItemDefinitions() {
        return itemDefinitions;
    }

    public void setItemDefinitions(List<ItemDefinition> itemDefinitions) {
        this.itemDefinitions = itemDefinitions;
    }

    public InterceptorChain getInterceptorChain() {
        return interceptorChain;
    }

    public void setInterceptorChain(InterceptorChain interceptorChain) {
        this.interceptorChain = interceptorChain;
    }

    public static final class JSONProperty2ItemParserBuilder {
        List<ItemDefinition> itemDefinitions;
        private InterceptorChain interceptorChain = new InterceptorChain();

        private JSONProperty2ItemParserBuilder() {
        }

        public JSONProperty2ItemParserBuilder(List<ItemDefinition> itemDefinitions) {
            this.itemDefinitions = itemDefinitions;
        }

        public JSONProperty2ItemParserBuilder(List<ItemDefinition> itemDefinitions,List<Interceptor> interceptors) {
            this.itemDefinitions = itemDefinitions;
            this.interceptorChain.addInterceptors(interceptors);
        }

        public JSONProperty2ItemParserBuilder interceptor(Interceptor interceptor) {
            this.interceptorChain.addInterceptor(interceptor);
            return this;
        }

        public JSONProperty2ItemParserBuilder interceptors(List<Interceptor> interceptors) {
            this.interceptorChain.addInterceptors(interceptors);
            return this;
        }

        public JSONProperty2ItemParser build() {
            return new JSONProperty2ItemParser(this);
        }
    }

}
