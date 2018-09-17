package com.android.internal.util;

public class ObjectUtils {
    private ObjectUtils() {
    }

    public static <T> T firstNotNull(T a, T b) {
        return a != null ? a : Preconditions.checkNotNull(b);
    }
}
