package com.android.org.bouncycastle.crypto.modes.gcm;

import com.android.org.bouncycastle.util.Pack;

public abstract class GCMUtil {
    private static final int E1 = -520093696;
    private static final long E1L = -2233785415175766016L;
    private static final int[] LOOKUP = generateLookup();

    private static int[] generateLookup() {
        int[] lookup = new int[256];
        for (int c = 0; c < 256; c++) {
            int v = 0;
            for (int i = 7; i >= 0; i--) {
                if (((1 << i) & c) != 0) {
                    v ^= E1 >>> (7 - i);
                }
            }
            lookup[c] = v;
        }
        return lookup;
    }

    public static byte[] oneAsBytes() {
        byte[] tmp = new byte[16];
        tmp[0] = Byte.MIN_VALUE;
        return tmp;
    }

    public static int[] oneAsInts() {
        int[] tmp = new int[4];
        tmp[0] = Integer.MIN_VALUE;
        return tmp;
    }

    public static long[] oneAsLongs() {
        long[] tmp = new long[2];
        tmp[0] = Long.MIN_VALUE;
        return tmp;
    }

    public static byte[] asBytes(int[] x) {
        byte[] z = new byte[16];
        Pack.intToBigEndian(x, z, 0);
        return z;
    }

    public static void asBytes(int[] x, byte[] z) {
        Pack.intToBigEndian(x, z, 0);
    }

    public static byte[] asBytes(long[] x) {
        byte[] z = new byte[16];
        Pack.longToBigEndian(x, z, 0);
        return z;
    }

    public static void asBytes(long[] x, byte[] z) {
        Pack.longToBigEndian(x, z, 0);
    }

    public static int[] asInts(byte[] x) {
        int[] z = new int[4];
        Pack.bigEndianToInt(x, 0, z);
        return z;
    }

    public static void asInts(byte[] x, int[] z) {
        Pack.bigEndianToInt(x, 0, z);
    }

    public static long[] asLongs(byte[] x) {
        long[] z = new long[2];
        Pack.bigEndianToLong(x, 0, z);
        return z;
    }

    public static void asLongs(byte[] x, long[] z) {
        Pack.bigEndianToLong(x, 0, z);
    }

    public static void multiply(byte[] x, byte[] y) {
        int[] t1 = asInts(x);
        multiply(t1, asInts(y));
        asBytes(t1, x);
    }

    public static void multiply(int[] x, int[] y) {
        int r00 = x[0];
        int r01 = x[1];
        int r02 = x[2];
        int r10 = 0;
        int r11 = 0;
        int r12 = 0;
        int r022 = 0;
        int r13 = x[3];
        int r012 = r02;
        int r002 = r01;
        int r013 = r00;
        int i = 0;
        while (i < 4) {
            int bits = y[i];
            int r03 = r13;
            int r132 = r022;
            int r023 = r012;
            int r014 = r002;
            int r003 = r013;
            for (int j = 0; j < 32; j++) {
                int m1 = bits >> 31;
                bits <<= 1;
                r10 ^= r003 & m1;
                r11 ^= r014 & m1;
                r12 ^= r023 & m1;
                r132 ^= r03 & m1;
                r03 = (r03 >>> 1) | (r023 << 31);
                r023 = (r023 >>> 1) | (r014 << 31);
                r014 = (r014 >>> 1) | (r003 << 31);
                r003 = (r003 >>> 1) ^ (((r03 << 31) >> 8) & E1);
            }
            i++;
            r013 = r003;
            r002 = r014;
            r012 = r023;
            r022 = r132;
            r13 = r03;
        }
        x[0] = r10;
        x[1] = r11;
        x[2] = r12;
        x[3] = r022;
    }

    public static void multiply(long[] x, long[] y) {
        long r00 = x[0];
        long r10 = 0;
        long r002 = 0;
        long r11 = x[1];
        long r01 = r00;
        int i = 0;
        while (i < 2) {
            long bits = y[i];
            long r012 = r11;
            long r112 = r002;
            long r003 = r01;
            for (int j = 0; j < 64; j++) {
                long m1 = bits >> 63;
                bits <<= 1;
                r10 ^= r003 & m1;
                r112 ^= r012 & m1;
                r012 = (r012 >>> 1) | (r003 << 63);
                r003 = (r003 >>> 1) ^ (((r012 << 63) >> 8) & E1L);
            }
            i++;
            r01 = r003;
            r002 = r112;
            r11 = r012;
        }
        x[0] = r10;
        x[1] = r002;
    }

    public static void multiplyP(int[] x) {
        x[0] = x[0] ^ (E1 & (shiftRight(x) >> 8));
    }

    public static void multiplyP(int[] x, int[] z) {
        z[0] = z[0] ^ (E1 & (shiftRight(x, z) >> 8));
    }

    public static void multiplyP8(int[] x) {
        x[0] = x[0] ^ LOOKUP[shiftRightN(x, 8) >>> 24];
    }

    public static void multiplyP8(int[] x, int[] y) {
        y[0] = y[0] ^ LOOKUP[shiftRightN(x, 8, y) >>> 24];
    }

    static int shiftRight(int[] x) {
        int b = x[0];
        x[0] = b >>> 1;
        int c = b << 31;
        int b2 = x[1];
        x[1] = (b2 >>> 1) | c;
        int c2 = b2 << 31;
        int b3 = x[2];
        x[2] = (b3 >>> 1) | c2;
        int c3 = b3 << 31;
        int b4 = x[3];
        x[3] = (b4 >>> 1) | c3;
        return b4 << 31;
    }

    static int shiftRight(int[] x, int[] z) {
        int b = x[0];
        z[0] = b >>> 1;
        int c = b << 31;
        int b2 = x[1];
        z[1] = (b2 >>> 1) | c;
        int c2 = b2 << 31;
        int b3 = x[2];
        z[2] = (b3 >>> 1) | c2;
        int c3 = b3 << 31;
        int b4 = x[3];
        z[3] = (b4 >>> 1) | c3;
        return b4 << 31;
    }

    static long shiftRight(long[] x) {
        long b = x[0];
        x[0] = b >>> 1;
        long c = b << 63;
        long b2 = x[1];
        x[1] = (b2 >>> 1) | c;
        return b2 << 63;
    }

    static long shiftRight(long[] x, long[] z) {
        long b = x[0];
        z[0] = b >>> 1;
        long c = b << 63;
        long b2 = x[1];
        z[1] = (b2 >>> 1) | c;
        return b2 << 63;
    }

    static int shiftRightN(int[] x, int n) {
        int b = x[0];
        int nInv = 32 - n;
        x[0] = b >>> n;
        int c = b << nInv;
        int b2 = x[1];
        x[1] = (b2 >>> n) | c;
        int c2 = b2 << nInv;
        int b3 = x[2];
        x[2] = (b3 >>> n) | c2;
        int c3 = b3 << nInv;
        int b4 = x[3];
        x[3] = (b4 >>> n) | c3;
        return b4 << nInv;
    }

    static int shiftRightN(int[] x, int n, int[] z) {
        int b = x[0];
        int nInv = 32 - n;
        z[0] = b >>> n;
        int c = b << nInv;
        int b2 = x[1];
        z[1] = (b2 >>> n) | c;
        int c2 = b2 << nInv;
        int b3 = x[2];
        z[2] = (b3 >>> n) | c2;
        int c3 = b3 << nInv;
        int b4 = x[3];
        z[3] = (b4 >>> n) | c3;
        return b4 << nInv;
    }

    public static void xor(byte[] x, byte[] y) {
        int i = 0;
        do {
            x[i] = (byte) (x[i] ^ y[i]);
            int i2 = i + 1;
            x[i2] = (byte) (x[i2] ^ y[i2]);
            int i3 = i2 + 1;
            x[i3] = (byte) (x[i3] ^ y[i3]);
            int i4 = i3 + 1;
            x[i4] = (byte) (x[i4] ^ y[i4]);
            i = i4 + 1;
        } while (i < 16);
    }

    public static void xor(byte[] x, byte[] y, int yOff, int yLen) {
        while (true) {
            yLen--;
            if (yLen >= 0) {
                x[yLen] = (byte) (x[yLen] ^ y[yOff + yLen]);
            } else {
                return;
            }
        }
    }

    public static void xor(byte[] x, byte[] y, byte[] z) {
        int i = 0;
        do {
            z[i] = (byte) (x[i] ^ y[i]);
            int i2 = i + 1;
            z[i2] = (byte) (x[i2] ^ y[i2]);
            int i3 = i2 + 1;
            z[i3] = (byte) (x[i3] ^ y[i3]);
            int i4 = i3 + 1;
            z[i4] = (byte) (x[i4] ^ y[i4]);
            i = i4 + 1;
        } while (i < 16);
    }

    public static void xor(int[] x, int[] y) {
        x[0] = x[0] ^ y[0];
        x[1] = x[1] ^ y[1];
        x[2] = x[2] ^ y[2];
        x[3] = x[3] ^ y[3];
    }

    public static void xor(int[] x, int[] y, int[] z) {
        z[0] = x[0] ^ y[0];
        z[1] = x[1] ^ y[1];
        z[2] = x[2] ^ y[2];
        z[3] = x[3] ^ y[3];
    }

    public static void xor(long[] x, long[] y) {
        x[0] = x[0] ^ y[0];
        x[1] = x[1] ^ y[1];
    }

    public static void xor(long[] x, long[] y, long[] z) {
        z[0] = x[0] ^ y[0];
        z[1] = x[1] ^ y[1];
    }
}
