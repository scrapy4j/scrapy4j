package scrapy4j.xxljob.argument;

import scrapy4j.xxljob.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListArgumentResolver extends AbstractArgumentResolver<List<Object>, Object,ListArgumentResolver> {
    @Override
    public Object resolve(Registry registry, List<Object> list) {
        List<Object> listRes =new ArrayList<>();
        list.forEach(li-> {
            for (Map.Entry<String, ArgumentResolver> resolverEntry : this.delegates.entrySet()) {
                listRes.add(resolverEntry.getValue().resolve(registry, li));
            }
        });
        return listRes;
    }


    public ListArgumentResolver argumentResolver(ArgumentResolver argumentResolver) {
        this.delegates.put("none", argumentResolver);
        return this;
    }
}
