package org.apache.xml.utils;

public class XMLCharacterRecognizer {
    public static boolean isWhiteSpace(char ch) {
        return ch == ' ' || ch == 9 || ch == 13 || ch == 10;
    }

    public static boolean isWhiteSpace(char[] ch, int start, int length) {
        int end = start + length;
        for (int s = start; s < end; s++) {
            if (!isWhiteSpace(ch[s])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWhiteSpace(StringBuffer buf) {
        int n = buf.length();
        for (int i = 0; i < n; i++) {
            if (!isWhiteSpace(buf.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWhiteSpace(String s) {
        if (s != null) {
            int n = s.length();
            for (int i = 0; i < n; i++) {
                if (!isWhiteSpace(s.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
