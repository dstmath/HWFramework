package android.icu.impl;

public final class PatternProps {
    private static final byte[] index2000 = {2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 6, 7, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 9};
    private static final byte[] latin1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 5, 5, 5, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 0, 3, 0, 3, 3, 0, 3, 0, 3, 3, 0, 0, 0, 0, 3, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final int[] syntax2000 = {0, -1, -65536, 2147418367, 2146435070, -65536, 4194303, -1048576, -242, 65537};
    private static final int[] syntaxOrWhiteSpace2000 = {0, -1, -16384, 2147419135, 2146435070, -65536, 4194303, -1048576, -242, 65537};

    public static boolean isSyntax(int c) {
        boolean z = false;
        if (c < 0) {
            return false;
        }
        if (c <= 255) {
            if (latin1[c] == 3) {
                z = true;
            }
            return z;
        } else if (c < 8208) {
            return false;
        } else {
            if (c <= 12336) {
                if (((syntax2000[index2000[(c - 8192) >> 5]] >> (c & 31)) & 1) != 0) {
                    z = true;
                }
                return z;
            } else if (64830 > c || c > 65094) {
                return false;
            } else {
                if (c <= 64831 || 65093 <= c) {
                    z = true;
                }
                return z;
            }
        }
    }

    public static boolean isSyntaxOrWhiteSpace(int c) {
        boolean z = false;
        if (c < 0) {
            return false;
        }
        if (c <= 255) {
            if (latin1[c] != 0) {
                z = true;
            }
            return z;
        } else if (c < 8206) {
            return false;
        } else {
            if (c <= 12336) {
                if (((syntaxOrWhiteSpace2000[index2000[(c - 8192) >> 5]] >> (c & 31)) & 1) != 0) {
                    z = true;
                }
                return z;
            } else if (64830 > c || c > 65094) {
                return false;
            } else {
                if (c <= 64831 || 65093 <= c) {
                    z = true;
                }
                return z;
            }
        }
    }

    public static boolean isWhiteSpace(int c) {
        boolean z = false;
        if (c < 0) {
            return false;
        }
        if (c <= 255) {
            if (latin1[c] == 5) {
                z = true;
            }
            return z;
        } else if (8206 > c || c > 8233) {
            return false;
        } else {
            if (c <= 8207 || 8232 <= c) {
                z = true;
            }
            return z;
        }
    }

    public static int skipWhiteSpace(CharSequence s, int i) {
        while (i < s.length() && isWhiteSpace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    public static String trimWhiteSpace(String s) {
        if (s.length() == 0 || (!isWhiteSpace(s.charAt(0)) && !isWhiteSpace(s.charAt(s.length() - 1)))) {
            return s;
        }
        int start = 0;
        int limit = s.length();
        while (start < limit && isWhiteSpace(s.charAt(start))) {
            start++;
        }
        if (start < limit) {
            while (isWhiteSpace(s.charAt(limit - 1))) {
                limit--;
            }
        }
        return s.substring(start, limit);
    }

    public static boolean isIdentifier(CharSequence s) {
        int limit = s.length();
        if (limit == 0) {
            return false;
        }
        int start = 0;
        while (true) {
            int start2 = start + 1;
            if (isSyntaxOrWhiteSpace(s.charAt(start)) != 0) {
                return false;
            }
            if (start2 >= limit) {
                return true;
            }
            start = start2;
        }
    }

    public static boolean isIdentifier(CharSequence s, int start, int limit) {
        if (start >= limit) {
            return false;
        }
        while (true) {
            int start2 = start + 1;
            if (isSyntaxOrWhiteSpace(s.charAt(start)) != 0) {
                return false;
            }
            if (start2 >= limit) {
                return true;
            }
            start = start2;
        }
    }

    public static int skipIdentifier(CharSequence s, int i) {
        while (i < s.length() && !isSyntaxOrWhiteSpace(s.charAt(i))) {
            i++;
        }
        return i;
    }
}
