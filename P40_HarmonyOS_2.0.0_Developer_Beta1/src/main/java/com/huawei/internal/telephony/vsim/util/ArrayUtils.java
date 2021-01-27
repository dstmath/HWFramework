package com.huawei.internal.telephony.vsim.util;

public final class ArrayUtils {
    private ArrayUtils() {
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(long[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(float[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(double[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    public static <T> T get(T[] array, int indexId, T def) {
        T tValue;
        if (!isEmpty(array) && indexId < array.length && indexId >= 0 && (tValue = array[indexId]) != null) {
            return tValue;
        }
        return def;
    }

    public static int get(int[] array, int indexId, int def) {
        if (!isEmpty(array) && indexId < array.length && indexId >= 0) {
            return array[indexId];
        }
        return def;
    }

    public static <T> int size(T[] array) {
        if (isEmpty(array)) {
            return 0;
        }
        return array.length;
    }

    public static int size(int[] array) {
        if (isEmpty(array)) {
            return 0;
        }
        return array.length;
    }
}
