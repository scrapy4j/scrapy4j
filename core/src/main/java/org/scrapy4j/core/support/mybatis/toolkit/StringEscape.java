package org.scrapy4j.core.support.mybatis.toolkit;

public class StringEscape {
    public StringEscape() {
    }

    private static boolean isEscapeNeededForString(String str, int len) {
        boolean needsHexEscape = false;

        for(int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            switch(c) {
            case '\u0000':
                needsHexEscape = true;
                break;
            case '\n':
                needsHexEscape = true;
                break;
            case '\r':
                needsHexEscape = true;
                break;
            case '\u001a':
                needsHexEscape = true;
                break;
            case '"':
                needsHexEscape = true;
                break;
            case '\'':
                needsHexEscape = true;
                break;
            case '\\':
                needsHexEscape = true;
            }

            if (needsHexEscape) {
                break;
            }
        }

        return needsHexEscape;
    }

    public static String escapeRawString(String escapeStr) {
        int stringLength = escapeStr.length();
        if (isEscapeNeededForString(escapeStr, stringLength)) {
            StringBuilder buf = new StringBuilder((int)((double)escapeStr.length() * 1.1D));

            for(int i = 0; i < stringLength; ++i) {
                char c = escapeStr.charAt(i);
                switch(c) {
                case '\u0000':
                    buf.append('\\');
                    buf.append('0');
                    break;
                case '\n':
                    buf.append('\\');
                    buf.append('n');
                    break;
                case '\r':
                    buf.append('\\');
                    buf.append('r');
                    break;
                case '\u001a':
                    buf.append('\\');
                    buf.append('Z');
                    break;
                case '"':
                    buf.append('\\');
                    buf.append('"');
                    break;
                case '\'':
                    buf.append('\\');
                    buf.append('\'');
                    break;
                case '\\':
                    buf.append('\\');
                    buf.append('\\');
                    break;
                default:
                    buf.append(c);
                }
            }

            return buf.toString();
        } else {
            return escapeStr;
        }
    }

    public static String escapeString(String escapeStr) {
        if (escapeStr.matches("'(.+)'")) {
            escapeStr = escapeStr.substring(1, escapeStr.length() - 1);
        }

        return "'" + escapeRawString(escapeStr) + "'";
    }
}
