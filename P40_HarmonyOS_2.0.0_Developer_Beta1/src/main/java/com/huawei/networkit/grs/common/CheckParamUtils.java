package com.huawei.networkit.grs.common;

public class CheckParamUtils {
    private static final String TAG = "CheckParamUtils";

    public static <T> T checkNotNull(T object, String message) {
        if (object != null) {
            return object;
        }
        throw new NullPointerException(message);
    }

    public static int checkNumParam(int number, int min, int max, int defaultNum, String message) {
        if (number > max || number < min) {
            return defaultNum;
        }
        Logger.d(TAG, message);
        return number;
    }

    public static long checkNumParam(long number, long min, long max, long defaultNum, String message) {
        if (number > max || number < min) {
            return defaultNum;
        }
        Logger.d(TAG, message);
        return number;
    }

    public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
}
