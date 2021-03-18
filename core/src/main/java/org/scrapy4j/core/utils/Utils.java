package org.scrapy4j.core.utils;


import cn.hutool.core.bean.BeanUtil;
import org.scrapy4j.core.resolver.FunctionResolver;
import org.scrapy4j.core.support.mybatis.toolkit.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.scripting.xmltags.OgnlCache;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static URI URI(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sleepMillis(long millis) {
        try {
            if (millis > 0) {
                TimeUnit.MILLISECONDS.sleep(millis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void logInfo(Logger logger, String info) {
        logger.info(String.format("[%s][%s]%s", Thread.currentThread().getName(), Thread.currentThread().getId(), info));
    }

    public static void logError(Logger logger, String info, Exception ex) {
        logger.error(String.format("[%s][%s]%s", Thread.currentThread().getName(), Thread.currentThread().getId(), info), ex);
    }

    public static boolean isMustacheVariable(String format) {
        Matcher matcher = Pattern.compile(mustacheRegex).matcher(format);
        if (matcher.find()) {
            return true;
        }
        matcher = Pattern.compile(doubleMustacheRegex).matcher(format);
        return matcher.find();
    }

    private static final String mustacheRegex = "\\{[^\\}]*\\}";
    private static final String doubleMustacheRegex = "\\{\\{[^\\}\\}]*\\}\\}";

    public static Object formatVars(String format, Object vars) {
        //{} expression match
        Object res = matchAndReplaceRegex(format, vars, mustacheRegex, 1);
        //{{}} expression match
        if (res instanceof String) {
            res = matchAndReplaceRegex(res.toString(), vars, doubleMustacheRegex, 2);
        }
        return res;
    }

    private static Object matchAndReplaceRegex(String format, Object vars, String regex, int braceWrapLength) {
        Matcher matcher = Pattern.compile(regex).matcher(format);
        Object res = format;
        while (matcher.find()) {
            String expression = matcher.group().substring(braceWrapLength, matcher.group().length() - braceWrapLength);
            Object value = null;
            try {
                value = OgnlCache.getValue(expression, vars);
            } catch (Exception ex) {
            }
            if (value != null) {
                if (matcher.group().length() == format.length()) {//单个占位符的替换返回object
                    res = value;
                } else {
                    res = res.toString().replace(matcher.group(), value.toString());
                }
            }
        }
        return res;
    }

    public static String SPEL_REGX = "#\\{@[^\\}]*\\}";

    public static boolean isSPEL(String expression) {
        boolean res = false;
        Matcher matcher = Pattern.compile(SPEL_REGX).matcher(expression);
        if (matcher.matches()) {
            res = true;
        }
        return res;
    }

//    public static LinkedHashSet<String> formatVars(LinkedHashSet<String> formatList, Object vars) {
//        LinkedHashSet<String> newList = new LinkedHashSet<>();
//        formatList.forEach(m -> newList.add(formatVars(m, vars)));
//        return newList;
//    }

    /**
     * Map参数处理,value值支持 {xxx}、Function函数、IFunctionResolver 三种写法
     *
     * @param resoureMap
     * @return
     */
    public static Map<String, Object> mapResolve(Map<String, Object> resoureMap) {
        Map<String, Object> targetMap = new LinkedHashMap<>();

        if (resoureMap != null && !resoureMap.isEmpty()) {

            BeanUtil.copyProperties(resoureMap, targetMap);
            Pattern pattern = Pattern.compile(mustacheRegex);

            // {xxx} 引用自身map的处理
            for (String key : targetMap.keySet()) {
                Object value = targetMap.get(key);
                if (value instanceof String) {
                    String newVal = String.valueOf(value);
                    Matcher matcher = pattern.matcher(newVal);
                    while (matcher.find()) {
                        String target = matcher.group();
                        String posKey = StringUtils.substring(target, 1, target.length() - 1);
                        if (targetMap.get(posKey) != null) {
                            //对应的值如果是Function 或者 IFunctionResolver 则直接先执行再取值
                            if (targetMap.get(posKey) instanceof Function) {
                                targetMap.put(posKey, ((Function<Object, String>) targetMap.get(posKey)).apply(targetMap));
                            } else if (targetMap.get(posKey) instanceof FunctionResolver) {
                                targetMap.put(posKey, ((FunctionResolver) targetMap.get(posKey)).resolve(targetMap).apply(targetMap));
                            }
                        }
                        if (targetMap.get(posKey) != null) {
                            newVal = StringUtils.replace(newVal, target, targetMap.get(posKey).toString());
                        }
                    }
                    targetMap.put(key, newVal);
                }
            }

            // 自身没有引用到的，剩余的Function 或者 IFunctionResolver的处理
            for (String key : targetMap.keySet()) {
                Object value = targetMap.get(key);
                if (value instanceof Function) {
                    targetMap.put(key, ((Function<Object, String>) value).apply(targetMap));
                } else if (value instanceof FunctionResolver) {
                    targetMap.put(key, ((FunctionResolver) value).resolve(targetMap).apply(targetMap));
                }
            }
        }
        return targetMap;
    }

    /**
     * 值映射为枚举
     *
     * @param enumClass 枚举类
     * @param value     枚举值
     * @param method    取值方法
     * @param <E>       对应枚举
     * @return
     */
    public static <E extends Enum<?>> E mappingEnum(Class<E> enumClass, Object value, Method method) {
        E[] es = enumClass.getEnumConstants();
        for (E e : es) {
            Object evalue;
            try {
                method.setAccessible(true);
                evalue = method.invoke(e);
            } catch (IllegalAccessException | InvocationTargetException e1) {
                throw ExceptionUtils.mpe("Error: NoSuchMethod in {}.  Cause:", e, enumClass.getName());
            }
            if (value instanceof Number && evalue instanceof Number
                    && new BigDecimal(String.valueOf(value)).compareTo(new BigDecimal(String.valueOf(evalue))) == 0) {
                return e;
            }
            if (Objects.equals(evalue, value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * map mapping obj
     *
     * @param maps
     * @param obj
     */
    public static void setFieldValue(Map<String, Object> maps, Object obj) {
        if (maps != null && !maps.isEmpty()) {
            Iterator<Map.Entry<String, Object>> iterator = maps.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> next = iterator.next();
                setFieldValue(next.getKey(), next.getValue(), obj);
            }
        }
    }

    public static void setFieldValue(String fieldName, Object value, Object obj) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {

        }
    }
}
