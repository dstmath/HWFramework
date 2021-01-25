package com.android.internal.util;

public class IntPair {
    private IntPair() {
    }

    public static long of(int first, int second) {
        return (((long) first) << 32) | (((long) second) & 4294967295L);
    }

    public static int first(long intPair) {
        return (int) (intPair >> 32);
    }

    public static int second(long intPair) {
        return (int) intPair;
    }
}
