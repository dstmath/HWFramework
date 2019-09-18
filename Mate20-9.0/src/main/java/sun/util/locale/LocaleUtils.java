package sun.util.locale;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LocaleUtils {
    private LocaleUtils() {
    }

    public static boolean caseIgnoreMatch(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        int len = s1.length();
        if (len != s2.length()) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (c1 != c2 && toLower(c1) != toLower(c2)) {
                return false;
            }
        }
        return true;
    }

    static int caseIgnoreCompare(String s1, String s2) {
        if (s1 == s2) {
            return 0;
        }
        return toLowerString(s1).compareTo(toLowerString(s2));
    }

    static char toUpper(char c) {
        return isLower(c) ? (char) (c - ' ') : c;
    }

    static char toLower(char c) {
        return isUpper(c) ? (char) (c + ' ') : c;
    }

    public static String toLowerString(String s) {
        int len = s.length();
        int i = 0;
        int idx = 0;
        while (idx < len && !isUpper(s.charAt(idx))) {
            idx++;
        }
        if (idx == len) {
            return s;
        }
        char[] buf = new char[len];
        while (i < len) {
            char c = s.charAt(i);
            buf[i] = i < idx ? c : toLower(c);
            i++;
        }
        return new String(buf);
    }

    static String toUpperString(String s) {
        int len = s.length();
        int i = 0;
        int idx = 0;
        while (idx < len && !isLower(s.charAt(idx))) {
            idx++;
        }
        if (idx == len) {
            return s;
        }
        char[] buf = new char[len];
        while (i < len) {
            char c = s.charAt(i);
            buf[i] = i < idx ? c : toUpper(c);
            i++;
        }
        return new String(buf);
    }

    static String toTitleString(String s) {
        int length = s.length();
        int len = length;
        if (length == 0) {
            return s;
        }
        int idx = 0;
        if (!isLower(s.charAt(0))) {
            idx = 1;
            while (idx < len && !isUpper(s.charAt(idx))) {
                idx++;
            }
        }
        if (idx == len) {
            return s;
        }
        char[] buf = new char[len];
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (i == 0 && idx == 0) {
                buf[i] = toUpper(c);
            } else if (i < idx) {
                buf[i] = c;
            } else {
                buf[i] = toLower(c);
            }
        }
        return new String(buf);
    }

    private static boolean isUpper(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private static boolean isLower(char c) {
        return c >= 'a' && c <= 'z';
    }

    static boolean isAlpha(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    static boolean isAlphaString(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!isAlpha(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }

    static boolean isNumericString(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!isNumeric(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isNumeric(c);
    }

    public static boolean isAlphaNumericString(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!isAlphaNumeric(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    static boolean isEmpty(Set<?> set) {
        return set == null || set.isEmpty();
    }

    static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}
