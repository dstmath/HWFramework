package com.huawei.opcollect.utils;

public final class StringUtils {
    private static final String TAG = "StringUtils";

    private StringUtils() {
        OPCollectLog.e(TAG, "static class should not initialize.");
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static int string2Int(String value, int defaultIfErr) {
        if (isEmpty(value)) {
            return defaultIfErr;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultIfErr;
        }
    }
}
