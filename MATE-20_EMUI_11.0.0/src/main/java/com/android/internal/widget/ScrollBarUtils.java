package com.android.internal.widget;

import android.annotation.UnsupportedAppUsage;

public class ScrollBarUtils {
    @UnsupportedAppUsage
    public static int getThumbLength(int size, int thickness, int extent, int range) {
        int minLength = thickness * 2;
        int length = Math.round((((float) size) * ((float) extent)) / ((float) range));
        return length < minLength ? minLength : length;
    }

    public static int getThumbOffset(int size, int thumbLength, int extent, int range, int offset) {
        int thumbOffset = Math.round((((float) (size - thumbLength)) * ((float) offset)) / ((float) (range - extent)));
        if (thumbOffset > size - thumbLength) {
            return size - thumbLength;
        }
        return thumbOffset;
    }
}
