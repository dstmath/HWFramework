package com.qq.taf.jce;

import java.nio.ByteBuffer;

public final class d {
    private static final byte[] mX;
    private static final byte[] mY;

    public static boolean a(boolean l, boolean r) {
        return l == r;
    }

    public static boolean a(short l, short r) {
        return l == r;
    }

    public static boolean equals(int l, int r) {
        return l == r;
    }

    public static boolean a(long l, long r) {
        return l == r;
    }

    public static boolean equals(float l, float r) {
        return l == r;
    }

    public static boolean equals(Object l, Object r) {
        return l.equals(r);
    }

    public static <T extends Comparable<T>> int a(T l, T r) {
        return l.compareTo(r);
    }

    public static byte[] a(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, bytes, 0, bytes.length);
        return bytes;
    }

    static {
        byte[] digits = new byte[]{(byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70};
        byte[] high = new byte[256];
        byte[] low = new byte[256];
        for (int i = 0; i < 256; i++) {
            high[i] = (byte) digits[i >>> 4];
            low[i] = (byte) digits[i & 15];
        }
        mX = high;
        mY = low;
    }
}
