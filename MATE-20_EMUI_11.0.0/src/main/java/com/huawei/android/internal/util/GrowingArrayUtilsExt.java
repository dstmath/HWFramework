package com.huawei.android.internal.util;

import com.android.internal.util.GrowingArrayUtils;

public class GrowingArrayUtilsExt {
    public static int[] insert(int[] array, int currentSize, int index, int element) {
        return GrowingArrayUtils.insert(array, currentSize, index, element);
    }

    public static int[] append(int[] array, int currentSize, int element) {
        return GrowingArrayUtils.append(array, currentSize, element);
    }
}
