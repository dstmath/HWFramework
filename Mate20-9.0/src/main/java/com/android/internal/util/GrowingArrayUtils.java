package com.android.internal.util;

import android.util.Log;

public final class GrowingArrayUtils {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    public static <T> T[] append(T[] array, int currentSize, T element) {
        if (currentSize + 1 > array.length) {
            T[] newArray = ArrayUtils.newUnpaddedArray(array.getClass().getComponentType(), growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static int[] append(int[] array, int currentSize, int element) {
        if (currentSize + 1 > array.length) {
            int[] newArray = ArrayUtils.newUnpaddedIntArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static long[] append(long[] array, int currentSize, long element) {
        if (currentSize + 1 > array.length) {
            long[] newArray = ArrayUtils.newUnpaddedLongArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static boolean[] append(boolean[] array, int currentSize, boolean element) {
        if (currentSize + 1 > array.length) {
            boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static float[] append(float[] array, int currentSize, float element) {
        if (currentSize + 1 > array.length) {
            float[] newArray = ArrayUtils.newUnpaddedFloatArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }

    public static <T> T[] insert(T[] array, int currentSize, int index, T element) {
        try {
            if (currentSize + 1 <= array.length) {
                System.arraycopy(array, index, array, index + 1, currentSize - index);
                array[index] = element;
                return array;
            }
            T[] newArray = ArrayUtils.newUnpaddedArray(array.getClass().getComponentType(), growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e("GrowingArrayUtils", "System.arraycopy error");
            return array;
        }
    }

    public static int[] insert(int[] array, int currentSize, int index, int element) {
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        int[] newArray = ArrayUtils.newUnpaddedIntArray(growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    public static long[] insert(long[] array, int currentSize, int index, long element) {
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        long[] newArray = ArrayUtils.newUnpaddedLongArray(growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    public static boolean[] insert(boolean[] array, int currentSize, int index, boolean element) {
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    public static int growSize(int currentSize) {
        if (currentSize <= 4) {
            return 8;
        }
        return currentSize * 2;
    }

    private GrowingArrayUtils() {
    }
}
