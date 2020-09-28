package com.huawei.internal.util;

import com.android.internal.util.ArrayUtils;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ArrayUtilsEx {
    public static <T> boolean isEmpty(T[] array) {
        return ArrayUtils.isEmpty(array);
    }

    public static <T> boolean contains(T[] array, T value) {
        return ArrayUtils.contains(array, value);
    }

    public static boolean contains(int[] array, int value) {
        return ArrayUtils.contains(array, value);
    }
}
