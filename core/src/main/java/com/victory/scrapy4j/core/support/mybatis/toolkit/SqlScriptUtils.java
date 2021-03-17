package com.victory.scrapy4j.core.support.mybatis.toolkit;

public final class SqlScriptUtils implements Constants {
    private SqlScriptUtils() {
    }

    public static String convertIf(final String sqlScript, final String ifTest, boolean newLine) {
        String newSqlScript = sqlScript;
        if (newLine) {
            newSqlScript = "\n" + sqlScript + "\n";
        }

        return String.format("<if test=\"%s\">%s</if>", ifTest, newSqlScript);
    }

    public static String convertTrim(final String sqlScript, final String prefix, final String suffix, final String prefixOverrides, final String suffixOverrides) {
        StringBuilder sb = new StringBuilder("<trim");
        if (StringUtils.isNotBlank(prefix)) {
            sb.append(" prefix=\"").append(prefix).append("\"");
        }

        if (StringUtils.isNotBlank(suffix)) {
            sb.append(" suffix=\"").append(suffix).append("\"");
        }

        if (StringUtils.isNotBlank(prefixOverrides)) {
            sb.append(" prefixOverrides=\"").append(prefixOverrides).append("\"");
        }

        if (StringUtils.isNotBlank(suffixOverrides)) {
            sb.append(" suffixOverrides=\"").append(suffixOverrides).append("\"");
        }

        return sb.append(">").append("\n").append(sqlScript).append("\n").append("</trim>").toString();
    }

    public static String convertChoose(final String whenTest, final String whenSqlScript, final String otherwise) {
        return "<choose>\n<when test=\"" + whenTest + "\"" + ">" + "\n" + whenSqlScript + "\n" + "</when>" + "\n" + "<otherwise>" + otherwise + "</otherwise>" + "\n" + "</choose>";
    }

    public static String convertForeach(final String sqlScript, final String collection, final String index, final String item, final String separator) {
        StringBuilder sb = new StringBuilder("<foreach");
        if (StringUtils.isNotBlank(collection)) {
            sb.append(" collection=\"").append(collection).append("\"");
        }

        if (StringUtils.isNotBlank(index)) {
            sb.append(" index=\"").append(index).append("\"");
        }

        if (StringUtils.isNotBlank(item)) {
            sb.append(" item=\"").append(item).append("\"");
        }

        if (StringUtils.isNotBlank(separator)) {
            sb.append(" separator=\"").append(separator).append("\"");
        }

        return sb.append(">").append("\n").append(sqlScript).append("\n").append("</foreach>").toString();
    }

    public static String convertWhere(final String sqlScript) {
        return "<where>\n" + sqlScript + "\n" + "</where>";
    }

    public static String convertSet(final String sqlScript) {
        return "<set>\n" + sqlScript + "\n" + "</set>";
    }

    public static String safeParam(final String param) {
        return "#{" + param + "}";
    }

    public static String unSafeParam(final String param) {
        return "${" + param + "}";
    }
}