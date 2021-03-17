package com.victory.scrapy4j.core.support.feign;

import com.alibaba.fastjson.JSONObject;
import com.victory.scrapy4j.core.component.pojo.Response;
import feign.FeignException;
import feign.codec.DecodeException;
import feign.optionals.OptionalDecoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Objects;

public class FeignDecoder implements feign.codec.Decoder {
    final feign.codec.Decoder delegate;

    public FeignDecoder(feign.codec.Decoder delegate) {
        Objects.requireNonNull(delegate, "Decoder must not be null. ");
        this.delegate = delegate;
    }

    @Override
    public Object decode(feign.Response response, Type type) throws IOException, DecodeException, FeignException {
        if (response != null
                && response.headers().get("content-type") != null
                && response.headers().get("content-type").contains("application/json")
                && springDecoder(delegate)) {
            type = JSONObject.class;
        } else {
            type = String.class;
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        for (String key : response.headers().keySet()) {
            headers.put(key, new LinkedList<>(response.headers().get(key)));
        }
        Object decodedObject = this.delegate.decode(response, type);
        return new Response(decodedObject, headers,response.status());
    }

    private boolean springDecoder(feign.codec.Decoder decoder) {
        return decoder instanceof OptionalDecoder;
    }
}
