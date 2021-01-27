package ohos.global.icu.impl;

import ohos.data.distributed.common.KvStore;

public final class PatternProps {
    private static final byte[] index2000 = {2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 6, 7, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9};
    private static final byte[] latin1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 5, 5, 5, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 0, 3, 0, 3, 3, 0, 3, 0, 3, 3, 0, 0, 0, 0, 3, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final int[] syntax2000 = {0, -1, -65536, 2147418367, 2146435070, -65536, KvStore.MAX_VALUE_LENGTH, -1048576, -242, 65537};
    private static final int[] syntaxOrWhiteSpace2000 = {0, -1, -16384, 2147419135, 2146435070, -65536, KvStore.MAX_VALUE_LENGTH, -1048576, -242, 65537};

    public static boolean isSyntax(int i) {
        if (i < 0) {
            return false;
        }
        if (i <= 255) {
            return latin1[i] == 3;
        }
        if (i < 8208) {
            return false;
        }
        if (i <= 12336) {
            return ((syntax2000[index2000[(i + -8192) >> 5]] >> (i & 31)) & 1) != 0;
        }
        if (64830 > i || i > 65094) {
            return false;
        }
        return i <= 64831 || 65093 <= i;
    }

    public static boolean isSyntaxOrWhiteSpace(int i) {
        if (i < 0) {
            return false;
        }
        if (i <= 255) {
            return latin1[i] != 0;
        }
        if (i < 8206) {
            return false;
        }
        if (i <= 12336) {
            return ((syntaxOrWhiteSpace2000[index2000[(i + -8192) >> 5]] >> (i & 31)) & 1) != 0;
        }
        if (64830 > i || i > 65094) {
            return false;
        }
        return i <= 64831 || 65093 <= i;
    }

    public static boolean isWhiteSpace(int i) {
        if (i < 0) {
            return false;
        }
        if (i <= 255) {
            return latin1[i] == 5;
        }
        if (8206 > i || i > 8233) {
            return false;
        }
        return i <= 8207 || 8232 <= i;
    }

    public static int skipWhiteSpace(CharSequence charSequence, int i) {
        while (i < charSequence.length() && isWhiteSpace(charSequence.charAt(i))) {
            i++;
        }
        return i;
    }

    public static String trimWhiteSpace(String str) {
        if (str.length() == 0) {
            return str;
        }
        int i = 0;
        if (!isWhiteSpace(str.charAt(0)) && !isWhiteSpace(str.charAt(str.length() - 1))) {
            return str;
        }
        int length = str.length();
        while (i < length && isWhiteSpace(str.charAt(i))) {
            i++;
        }
        if (i < length) {
            while (isWhiteSpace(str.charAt(length - 1))) {
                length--;
            }
        }
        return str.substring(i, length);
    }

    public static boolean isIdentifier(CharSequence charSequence) {
        int length = charSequence.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        while (true) {
            int i2 = i + 1;
            if (isSyntaxOrWhiteSpace(charSequence.charAt(i))) {
                return false;
            }
            if (i2 >= length) {
                return true;
            }
            i = i2;
        }
    }

    public static boolean isIdentifier(CharSequence charSequence, int i, int i2) {
        if (i >= i2) {
            return false;
        }
        while (true) {
            int i3 = i + 1;
            if (isSyntaxOrWhiteSpace(charSequence.charAt(i))) {
                return false;
            }
            if (i3 >= i2) {
                return true;
            }
            i = i3;
        }
    }

    public static int skipIdentifier(CharSequence charSequence, int i) {
        while (i < charSequence.length() && !isSyntaxOrWhiteSpace(charSequence.charAt(i))) {
            i++;
        }
        return i;
    }
}
