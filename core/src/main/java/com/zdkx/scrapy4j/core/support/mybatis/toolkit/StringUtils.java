package com.zdkx.scrapy4j.core.support.mybatis.toolkit;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StringUtils {
    public static final String EMPTY = "";
    public static final String IS = "is";
    public static final char UNDERLINE = '_';
    public static final Pattern MP_SQL_PLACE_HOLDER = Pattern.compile("[{](?<idx>\\d+)}");
    private static final Pattern P_IS_COLUMN = Pattern.compile("^\\w\\S*[\\w\\d]*$");
    private static final Pattern CAPITAL_MODE = Pattern.compile("^[0-9A-Z/_]+$");

    private StringUtils() {
    }

    public static String format(String target, Object... params) {
        return String.format(target, params);
    }

    public static String blob2String(Blob blob) {
        if (null != blob) {
            try {
                byte[] returnValue = blob.getBytes(1L, (int) blob.length());
                return new String(returnValue, StandardCharsets.UTF_8);
            } catch (Exception var2) {
                throw ExceptionUtils.mpe("Blob Convert To String Error!", new Object[0]);
            }
        } else {
            return null;
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static boolean isEmpty(CharSequence cs) {
        return isBlank(cs);
    }

    public static boolean isBlank(final CharSequence cs) {
        if (cs == null) {
            return true;
        } else {
            int l = cs.length();
            if (l > 0) {
                for (int i = 0; i < l; ++i) {
                    if (!Character.isWhitespace(cs.charAt(i))) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static String guessGetterName(String name, Class<?> type) {
        return Boolean.TYPE == type ? (name.startsWith("is") ? name : "is" + upperFirst(name)) : "get" + upperFirst(name);
    }

    public static String upperFirst(String src) {
        if (Character.isLowerCase(src.charAt(0))) {
            return 1 == src.length() ? src.toUpperCase() : Character.toUpperCase(src.charAt(0)) + src.substring(1);
        } else {
            return src;
        }
    }

    public static boolean isCamel(String str) {
        return str.contains("_") ? false : Character.isLowerCase(str.charAt(0));
    }

    public static boolean isNotColumnName(String str) {
        return !P_IS_COLUMN.matcher(str).matches();
    }

    public static String getTargetColumn(String column) {
        return isNotColumnName(column) ? column.substring(1, column.length() - 1) : column;
    }

    public static String camelToUnderline(String param) {
        if (isBlank(param)) {
            return "";
        } else {
            int len = param.length();
            StringBuilder sb = new StringBuilder(len);

            for (int i = 0; i < len; ++i) {
                char c = param.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    sb.append('_');
                }

                sb.append(Character.toLowerCase(c));
            }

            return sb.toString();
        }
    }

    public static String resolveFieldName(String getMethodName) {
        if (getMethodName.startsWith("get")) {
            getMethodName = getMethodName.substring(3);
        } else if (getMethodName.startsWith("is")) {
            getMethodName = getMethodName.substring(2);
        }

        return firstToLowerCase(getMethodName);
    }

    public static String underlineToCamel(String param) {
        if (isBlank(param)) {
            return "";
        } else {
            String temp = param.toLowerCase();
            int len = temp.length();
            StringBuilder sb = new StringBuilder(len);

            for (int i = 0; i < len; ++i) {
                char c = temp.charAt(i);
                if (c == '_') {
                    ++i;
                    if (i < len) {
                        sb.append(Character.toUpperCase(temp.charAt(i)));
                    }
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        }
    }

    public static String firstToLowerCase(String param) {
        return isBlank(param) ? "" : param.substring(0, 1).toLowerCase() + param.substring(1);
    }

    public static boolean isUpperCase(String str) {
        return matches("^[A-Z]+$", str);
    }

    public static boolean matches(String regex, String input) {
        return null != regex && null != input ? Pattern.matches(regex, input) : false;
    }

    public static String sqlArgsFill(String content, Object... args) {
        if (isNotBlank(content) && ArrayUtils.isNotEmpty(args)) {
            BiIntFunction<Matcher, CharSequence> handler = (m, i) -> {
                return sqlParam(args[Integer.parseInt(m.group("idx"))]);
            };
            return replace(content, MP_SQL_PLACE_HOLDER, handler).toString();
        } else {
            return content;
        }
    }

    public static StringBuilder replace(CharSequence src, Pattern ptn, BiIntFunction<Matcher, CharSequence> replacer) {
        int idx = 0;
        int last = 0;
        int len = src.length();
        Matcher m = ptn.matcher(src);

        StringBuilder sb;
        for (sb = new StringBuilder(); m.find(); last = m.end()) {
            sb.append(src, last, m.start()).append(replacer.apply(m, idx++));
        }

        if (last < len) {
            sb.append(src, last, len);
        }

        return sb;
    }

    public static String sqlParam(Object obj) {
        String repStr;
        if (obj instanceof Collection) {
            repStr = quotaMarkList((Collection) obj);
        } else {
            repStr = quotaMark(obj);
        }

        return repStr;
    }

    public static String quotaMark(Object obj) {
        String srcStr = String.valueOf(obj);
        return obj instanceof CharSequence ? StringEscape.escapeString(srcStr) : srcStr;
    }

    public static String quotaMarkList(Collection<?> coll) {
        return coll.stream().map(StringUtils::quotaMark).collect(Collectors.joining(",", "(", ")"));
    }

    public static String concatCapitalize(String concatStr, final String str) {
        if (isBlank(concatStr)) {
            concatStr = "";
        }

        if (str != null && str.length() != 0) {
            char firstChar = str.charAt(0);
            return Character.isTitleCase(firstChar) ? str : concatStr + Character.toTitleCase(firstChar) + str.substring(1);
        } else {
            return str;
        }
    }

    public static String capitalize(final String str) {
        return concatCapitalize((String) null, str);
    }

    public static boolean checkValNotNull(Object object) {
        if (object instanceof CharSequence) {
            return isNotBlank((CharSequence) object);
        } else {
            return object != null;
        }
    }

    public static boolean checkValNull(Object object) {
        return !checkValNotNull(object);
    }

    public static boolean containsUpperCase(String word) {
        for (int i = 0; i < word.length(); ++i) {
            char c = word.charAt(i);
            if (Character.isUpperCase(c)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCapitalMode(String word) {
        return null != word && CAPITAL_MODE.matcher(word).matches();
    }

    public static boolean isMixedMode(String word) {
        return matches(".*[A-Z]+.*", word) && matches(".*[/_]+.*", word);
    }

    public static boolean endsWith(String str, String suffix) {
        return endsWith(str, suffix, false);
    }

    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return endsWith(str, suffix, true);
    }

    private static boolean endsWith(String str, String suffix, boolean ignoreCase) {
        if (str != null && suffix != null) {
            if (suffix.length() > str.length()) {
                return false;
            } else {
                int strOffset = str.length() - suffix.length();
                return str.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
            }
        } else {
            return str == null && suffix == null;
        }
    }

    public static String[] split(final String str, final String separatorChars) {
        List<String> strings = splitWorker(str, separatorChars, -1, false);
        return strings.toArray(new String[0]);
    }

    public static List<String> splitWorker(final String str, final String separatorChars, final int max, final boolean preserveAllTokens) {
        if (str == null) {
            return null;
        } else {
            int len = str.length();
            if (len == 0) {
                return Collections.emptyList();
            } else {
                List<String> list = new ArrayList();
                int sizePlus1 = 1;
                int i = 0;
                int start = 0;
                boolean match = false;
                boolean lastMatch = false;
                if (separatorChars != null) {
                    if (separatorChars.length() != 1) {
                        label87:
                        while (true) {
                            while (true) {
                                if (i >= len) {
                                    break label87;
                                }

                                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                                    if (match || preserveAllTokens) {
                                        lastMatch = true;
                                        if (sizePlus1++ == max) {
                                            i = len;
                                            lastMatch = false;
                                        }

                                        list.add(str.substring(start, i));
                                        match = false;
                                    }

                                    ++i;
                                    start = i;
                                } else {
                                    lastMatch = false;
                                    match = true;
                                    ++i;
                                }
                            }
                        }
                    } else {
                        char sep = separatorChars.charAt(0);

                        label71:
                        while (true) {
                            while (true) {
                                if (i >= len) {
                                    break label71;
                                }

                                if (str.charAt(i) == sep) {
                                    if (match || preserveAllTokens) {
                                        lastMatch = true;
                                        if (sizePlus1++ == max) {
                                            i = len;
                                            lastMatch = false;
                                        }

                                        list.add(str.substring(start, i));
                                        match = false;
                                    }

                                    ++i;
                                    start = i;
                                } else {
                                    lastMatch = false;
                                    match = true;
                                    ++i;
                                }
                            }
                        }
                    }
                } else {
                    label103:
                    while (true) {
                        while (true) {
                            if (i >= len) {
                                break label103;
                            }

                            if (Character.isWhitespace(str.charAt(i))) {
                                if (match || preserveAllTokens) {
                                    lastMatch = true;
                                    if (sizePlus1++ == max) {
                                        i = len;
                                        lastMatch = false;
                                    }

                                    list.add(str.substring(start, i));
                                    match = false;
                                }

                                ++i;
                                start = i;
                            } else {
                                lastMatch = false;
                                match = true;
                                ++i;
                            }
                        }
                    }
                }

                if (match || preserveAllTokens && lastMatch) {
                    list.add(str.substring(start, i));
                }

                return list;
            }
        }
    }

    public static boolean isCharSequence(Class<?> clazz) {
        return clazz != null && CharSequence.class.isAssignableFrom(clazz);
    }

    public static String removeIsPrefixIfBoolean(String propertyName, Class<?> propertyType) {
        if (ClassUtils.isBoolean(propertyType) && propertyName.startsWith("is")) {
            String property = propertyName.replaceFirst("is", "");
            if (isBlank(property)) {
                return propertyName;
            } else {
                String firstCharToLowerStr = firstCharToLower(property);
                return property.equals(firstCharToLowerStr) ? propertyName : firstCharToLowerStr;
            }
        } else {
            return propertyName;
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static boolean isBoolean(Class<?> propertyCls) {
        return propertyCls != null && (Boolean.TYPE.isAssignableFrom(propertyCls) || Boolean.class.isAssignableFrom(propertyCls));
    }

    public static String firstCharToLower(String rawString) {
        return prefixToLower(rawString, 1);
    }

    public static String prefixToLower(String rawString, int index) {
        String beforeChar = rawString.substring(0, index).toLowerCase();
        String afterChar = rawString.substring(index);
        return beforeChar + afterChar;
    }

    public static String removePrefixAfterPrefixToLower(String rawString, int index) {
        return prefixToLower(rawString.substring(index), 1);
    }

    public static String camelToHyphen(String input) {
        return wordsToHyphenCase(wordsAndHyphenAndCamelToConstantCase(input));
    }

    private static String wordsAndHyphenAndCamelToConstantCase(String input) {
        boolean betweenUpperCases = false;
        boolean containsLowerCase = containsLowerCase(input);
        StringBuilder buf = new StringBuilder();
        char previousChar = ' ';
        char[] chars = input.toCharArray();
        char[] var6 = chars;
        int var7 = chars.length;

        for (int var8 = 0; var8 < var7; ++var8) {
            char c = var6[var8];
            boolean isUpperCaseAndPreviousIsUpperCase = Character.isUpperCase(previousChar) && Character.isUpperCase(c);
            boolean isUpperCaseAndPreviousIsLowerCase = Character.isLowerCase(previousChar) && Character.isUpperCase(c);
            boolean previousIsWhitespace = Character.isWhitespace(previousChar);
            boolean lastOneIsNotUnderscore = buf.length() > 0 && buf.charAt(buf.length() - 1) != '_';
            boolean isNotUnderscore = c != '_';
            if (!lastOneIsNotUnderscore || !isUpperCaseAndPreviousIsLowerCase && !previousIsWhitespace && (!betweenUpperCases || !containsLowerCase || !isUpperCaseAndPreviousIsUpperCase)) {
                if (Character.isDigit(previousChar) && Character.isLetter(c)) {
                    buf.append('_');
                }
            } else {
                buf.append("_");
            }

            if (shouldReplace(c) && lastOneIsNotUnderscore) {
                buf.append('_');
            } else if (!Character.isWhitespace(c) && (isNotUnderscore || lastOneIsNotUnderscore)) {
                buf.append(Character.toUpperCase(c));
            }

            previousChar = c;
        }

        if (Character.isWhitespace(previousChar)) {
            buf.append("_");
        }

        return buf.toString();
    }

    public static boolean containsLowerCase(String s) {
        char[] var1 = s.toCharArray();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            char c = var1[var3];
            if (Character.isLowerCase(c)) {
                return true;
            }
        }

        return false;
    }

    private static boolean shouldReplace(char c) {
        return c == '.' || c == '_' || c == '-';
    }

    private static String wordsToHyphenCase(String s) {
        StringBuilder buf = new StringBuilder();
        char lastChar = 'a';
        char[] var3 = s.toCharArray();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            char c = var3[var5];
            if (Character.isWhitespace(lastChar) && !Character.isWhitespace(c) && '-' != c && buf.length() > 0 && buf.charAt(buf.length() - 1) != '-') {
                buf.append("-");
            }

            if ('_' == c) {
                buf.append('-');
            } else if ('.' == c) {
                buf.append('-');
            } else if (!Character.isWhitespace(c)) {
                buf.append(Character.toLowerCase(c));
            }

            lastChar = c;
        }

        if (Character.isWhitespace(lastChar)) {
            buf.append("-");
        }

        return buf.toString();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static String removeWordWithComma(String s, String p) {
        String match = "\\s*" + p + "\\s*,{0,1}";
        return s.replaceAll(match, "");
    }
}
