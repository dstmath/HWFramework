package com.android.org.conscrypt;

final class ArrayUtils {
    private ArrayUtils() {
    }

    static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException("length=" + arrayLength + "; regionStart=" + offset + "; regionLength=" + count);
        }
    }
}
