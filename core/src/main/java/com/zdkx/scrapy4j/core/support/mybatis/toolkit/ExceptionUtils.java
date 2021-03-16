package com.zdkx.scrapy4j.core.support.mybatis.toolkit;

public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static MybatisPlusException mpe(String msg, Throwable t, Object... params) {
        return new MybatisPlusException(StringUtils.format(msg, params), t);
    }

    public static MybatisPlusException mpe(String msg, Object... params) {
        return new MybatisPlusException(StringUtils.format(msg, params));
    }

    public static MybatisPlusException mpe(Throwable t) {
        return new MybatisPlusException(t);
    }
}
