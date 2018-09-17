package sun.misc;

import java.util.Comparator;

public class ASCIICaseInsensitiveComparator implements Comparator<String> {
    static final /* synthetic */ boolean -assertionsDisabled = (ASCIICaseInsensitiveComparator.class.desiredAssertionStatus() ^ 1);
    public static final Comparator<String> CASE_INSENSITIVE_ORDER = new ASCIICaseInsensitiveComparator();

    public int compare(String s1, String s2) {
        int n1 = s1.length();
        int n2 = s2.length();
        int minLen = n1 < n2 ? n1 : n2;
        int i = 0;
        while (i < minLen) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (-assertionsDisabled || (c1 <= 127 && c2 <= 127)) {
                if (c1 != c2) {
                    c1 = (char) toLower(c1);
                    c2 = (char) toLower(c2);
                    if (c1 != c2) {
                        return c1 - c2;
                    }
                }
                i++;
            } else {
                throw new AssertionError();
            }
        }
        return n1 - n2;
    }

    public static int lowerCaseHashCode(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = (h * 31) + toLower(s.charAt(i));
        }
        return h;
    }

    static boolean isLower(int ch) {
        return ((ch + -97) | (122 - ch)) >= 0;
    }

    static boolean isUpper(int ch) {
        return ((ch + -65) | (90 - ch)) >= 0;
    }

    static int toLower(int ch) {
        return isUpper(ch) ? ch + 32 : ch;
    }

    static int toUpper(int ch) {
        return isLower(ch) ? ch - 32 : ch;
    }
}
