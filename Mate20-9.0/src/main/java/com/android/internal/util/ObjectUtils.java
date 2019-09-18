package com.android.internal.util;

public class ObjectUtils {
    private ObjectUtils() {
    }

    public static <T> T firstNotNull(T a, T b) {
        return a != null ? a : Preconditions.checkNotNull(b);
    }

    public static <T extends Comparable> int compare(T a, T b) {
        if (a != null) {
            return b != null ? a.compareTo(b) : 1;
        }
        return b != null ? -1 : 0;
    }
}
