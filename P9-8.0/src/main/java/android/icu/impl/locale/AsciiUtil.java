package android.icu.impl.locale;

import android.icu.impl.Utility;

public final class AsciiUtil {

    public static class CaseInsensitiveKey {
        private int _hash;
        private String _key;

        public CaseInsensitiveKey(String key) {
            this._key = key;
            this._hash = AsciiUtil.toLowerString(key).hashCode();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof CaseInsensitiveKey) {
                return AsciiUtil.caseIgnoreMatch(this._key, ((CaseInsensitiveKey) o)._key);
            }
            return false;
        }

        public int hashCode() {
            return this._hash;
        }
    }

    public static boolean caseIgnoreMatch(String s1, String s2) {
        boolean z = true;
        if (Utility.sameObjects(s1, s2)) {
            return true;
        }
        int len = s1.length();
        if (len != s2.length()) {
            return false;
        }
        int i = 0;
        while (i < len) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (c1 != c2 && toLower(c1) != toLower(c2)) {
                break;
            }
            i++;
        }
        if (i != len) {
            z = false;
        }
        return z;
    }

    public static int caseIgnoreCompare(String s1, String s2) {
        if (Utility.sameObjects(s1, s2)) {
            return 0;
        }
        return toLowerString(s1).compareTo(toLowerString(s2));
    }

    public static char toUpper(char c) {
        if (c < 'a' || c > 'z') {
            return c;
        }
        return (char) (c - 32);
    }

    public static char toLower(char c) {
        if (c < 'A' || c > 'Z') {
            return c;
        }
        return (char) (c + 32);
    }

    public static String toLowerString(String s) {
        int idx = 0;
        while (idx < s.length()) {
            char c = s.charAt(idx);
            if (c >= 'A' && c <= 'Z') {
                break;
            }
            idx++;
        }
        if (idx == s.length()) {
            return s;
        }
        StringBuilder buf = new StringBuilder(s.substring(0, idx));
        while (idx < s.length()) {
            buf.append(toLower(s.charAt(idx)));
            idx++;
        }
        return buf.toString();
    }

    public static String toUpperString(String s) {
        int idx = 0;
        while (idx < s.length()) {
            char c = s.charAt(idx);
            if (c >= 'a' && c <= 'z') {
                break;
            }
            idx++;
        }
        if (idx == s.length()) {
            return s;
        }
        StringBuilder buf = new StringBuilder(s.substring(0, idx));
        while (idx < s.length()) {
            buf.append(toUpper(s.charAt(idx)));
            idx++;
        }
        return buf.toString();
    }

    public static String toTitleString(String s) {
        if (s.length() == 0) {
            return s;
        }
        int idx = 0;
        char c = s.charAt(0);
        if (c < 'a' || c > 'z') {
            idx = 1;
            while (idx < s.length() && (c < 'A' || c > 'Z')) {
                idx++;
            }
        }
        if (idx == s.length()) {
            return s;
        }
        StringBuilder buf = new StringBuilder(s.substring(0, idx));
        if (idx == 0) {
            buf.append(toUpper(s.charAt(idx)));
            idx++;
        }
        while (idx < s.length()) {
            buf.append(toLower(s.charAt(idx)));
            idx++;
        }
        return buf.toString();
    }

    public static boolean isAlpha(char c) {
        if (c < 'A' || c > 'Z') {
            return c >= 'a' && c <= 'z';
        } else {
            return true;
        }
    }

    public static boolean isAlphaString(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isAlpha(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isNumericString(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isNumeric(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAlphaNumeric(char c) {
        return !isAlpha(c) ? isNumeric(c) : true;
    }

    public static boolean isAlphaNumericString(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isAlphaNumeric(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
