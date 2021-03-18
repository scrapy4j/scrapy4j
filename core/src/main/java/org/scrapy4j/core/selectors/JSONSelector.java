package org.scrapy4j.core.selectors;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONSelector implements Selector {
    private String path;
    private JSON jsonObject;

    public JSONSelector(JSON jsonObject, String path) {
        this.path = path;
        this.jsonObject = jsonObject;
    }

    @Override
    public Map<String, Object> get() {
        cn.hutool.json.JSONObject dataObj = (cn.hutool.json.JSONObject) JSONUtil.getByPath(jsonObject, path);
        return JSONObject.parseObject(dataObj.toJSONString(0), new TypeReference<Map<String, Object>>() {
        });
    }

    private String LIST_REGX = "\\[\\*\\]";
    private String LIST_EXP = "[*]";

    @Override
    public List<Object> getAll() {
        List<Object> values = new ArrayList<>();
        Matcher matcher = Pattern.compile(LIST_REGX).matcher(this.path);
        List<String> path = new ArrayList<>();
        path.add(this.path);
        while (matcher.find()) {
            // calculate maxLength from the data
            int maxLength = calculateMaxLength(matcher.start());

            List<String> subPath = new ArrayList<>();
            for (int i = 0; i < maxLength; i++) {
                int finalI = i;
                path.forEach(m -> subPath.add(m.replaceFirst(LIST_REGX, String.format("[%s]", finalI))));
            }
            path.addAll(subPath);
        }
        List<String> removePath = new ArrayList<>();
        path.forEach(m -> {
            if (m.contains(LIST_EXP)) {
                removePath.add(m);
            }
        });
        path.removeAll(removePath);
        //TODO replace to ognl(mybatis) ,for less dependency
        path.forEach(m -> values.add(JSONUtil.getByPath(jsonObject, m)));
        return values;
    }

    /**
     * 计算
     */
    private int calculateMaxLength(int laseEndIndex) {
        int maxLength = 1;
        int index = 1;
        String subPath = "";
        JSONArray arr = null;
        int endIndex = 0;
        int startIndex = 0;
        boolean first = true;
        while (endIndex < laseEndIndex || (laseEndIndex == 0 && first)) {
            if (!first) {
                // 开始索引等于结束索引加上LIST_EXP的长度与.的长度
                startIndex = endIndex + LIST_EXP.length() + 1;
            }

            endIndex = StringUtils.ordinalIndexOf(this.path, LIST_EXP, index);
            subPath = this.path.substring(startIndex, endIndex);
            if (StringUtils.isBlank(subPath)) {
                arr = (JSONArray) jsonObject;
            } else {
                arr = (JSONArray) JSONUtil.getByPath(arr != null ? arr.getJSONObject(0) : jsonObject, subPath);
            }

            if (arr != null && arr.size() > 0) {
                maxLength = maxLength * arr.size();
            }

            index++;
            first = false;
        }

        return maxLength;
    }
}
