package android.icu.impl;

public final class PatternProps {
    private static final byte[] index2000 = new byte[]{(byte) 2, (byte) 3, (byte) 4, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 6, (byte) 7, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 8, (byte) 9};
    private static final byte[] latin1 = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 0, (byte) 3, (byte) 0, (byte) 3, (byte) 3, (byte) 0, (byte) 3, (byte) 0, (byte) 3, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    private static final int[] syntax2000 = new int[]{0, -1, -65536, 2147418367, 2146435070, -65536, 4194303, -1048576, -242, 65537};
    private static final int[] syntaxOrWhiteSpace2000 = new int[]{0, -1, -16384, 2147419135, 2146435070, -65536, 4194303, -1048576, -242, 65537};

    public static boolean isSyntax(int c) {
        boolean z = true;
        if (c < 0) {
            return false;
        }
        if (c <= 255) {
            if (latin1[c] != (byte) 3) {
                z = false;
            }
            return z;
        } else if (c < 8208) {
            return false;
        } else {
            if (c <= 12336) {
                if (((syntax2000[index2000[(c - 8192) >> 5]] >> (c & 31)) & 1) == 0) {
                    z = false;
                }
                return z;
            } else if (64830 > c || c > 65094) {
                return false;
            } else {
                if (c > 64831 && 65093 > c) {
                    z = false;
                }
                return z;
            }
        }
    }

    public static boolean isSyntaxOrWhiteSpace(int c) {
        boolean z = true;
        if (c < 0) {
            return false;
        }
        if (c <= 255) {
            if (latin1[c] == (byte) 0) {
                z = false;
            }
            return z;
        } else if (c < 8206) {
            return false;
        } else {
            if (c <= 12336) {
                if (((syntaxOrWhiteSpace2000[index2000[(c - 8192) >> 5]] >> (c & 31)) & 1) == 0) {
                    z = false;
                }
                return z;
            } else if (64830 > c || c > 65094) {
                return false;
            } else {
                if (c > 64831 && 65093 > c) {
                    z = false;
                }
                return z;
            }
        }
    }

    public static boolean isWhiteSpace(int c) {
        boolean z = true;
        if (c < 0) {
            return false;
        }
        if (c <= 255) {
            if (latin1[c] != (byte) 5) {
                z = false;
            }
            return z;
        } else if (8206 > c || c > 8233) {
            return false;
        } else {
            if (c > 8207 && 8232 > c) {
                z = false;
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
        if (s.length() == 0 || (!isWhiteSpace(s.charAt(0)) && (isWhiteSpace(s.charAt(s.length() - 1)) ^ 1) != 0)) {
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
            if (isSyntaxOrWhiteSpace(s.charAt(start))) {
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
            if (isSyntaxOrWhiteSpace(s.charAt(start))) {
                return false;
            }
            if (start2 >= limit) {
                return true;
            }
            start = start2;
        }
    }

    public static int skipIdentifier(CharSequence s, int i) {
        while (i < s.length() && (isSyntaxOrWhiteSpace(s.charAt(i)) ^ 1) != 0) {
            i++;
        }
        return i;
    }
}
