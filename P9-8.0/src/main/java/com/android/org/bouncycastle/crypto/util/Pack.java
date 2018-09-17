package com.android.org.bouncycastle.crypto.util;

public abstract class Pack {
    public static int bigEndianToInt(byte[] bs, int off) {
        off++;
        off++;
        return (((bs[off] << 24) | ((bs[off] & 255) << 16)) | ((bs[off] & 255) << 8)) | (bs[off + 1] & 255);
    }

    public static void bigEndianToInt(byte[] bs, int off, int[] ns) {
        for (int i = 0; i < ns.length; i++) {
            ns[i] = bigEndianToInt(bs, off);
            off += 4;
        }
    }

    public static byte[] intToBigEndian(int n) {
        byte[] bs = new byte[4];
        intToBigEndian(n, bs, 0);
        return bs;
    }

    public static void intToBigEndian(int n, byte[] bs, int off) {
        bs[off] = (byte) (n >>> 24);
        off++;
        bs[off] = (byte) (n >>> 16);
        off++;
        bs[off] = (byte) (n >>> 8);
        bs[off + 1] = (byte) n;
    }

    public static byte[] intToBigEndian(int[] ns) {
        byte[] bs = new byte[(ns.length * 4)];
        intToBigEndian(ns, bs, 0);
        return bs;
    }

    public static void intToBigEndian(int[] ns, byte[] bs, int off) {
        for (int intToBigEndian : ns) {
            intToBigEndian(intToBigEndian, bs, off);
            off += 4;
        }
    }

    public static long bigEndianToLong(byte[] bs, int off) {
        return ((((long) bigEndianToInt(bs, off)) & 4294967295L) << 32) | (((long) bigEndianToInt(bs, off + 4)) & 4294967295L);
    }

    public static void bigEndianToLong(byte[] bs, int off, long[] ns) {
        for (int i = 0; i < ns.length; i++) {
            ns[i] = bigEndianToLong(bs, off);
            off += 8;
        }
    }

    public static byte[] longToBigEndian(long n) {
        byte[] bs = new byte[8];
        longToBigEndian(n, bs, 0);
        return bs;
    }

    public static void longToBigEndian(long n, byte[] bs, int off) {
        intToBigEndian((int) (n >>> 32), bs, off);
        intToBigEndian((int) (4294967295L & n), bs, off + 4);
    }

    public static byte[] longToBigEndian(long[] ns) {
        byte[] bs = new byte[(ns.length * 8)];
        longToBigEndian(ns, bs, 0);
        return bs;
    }

    public static void longToBigEndian(long[] ns, byte[] bs, int off) {
        for (long longToBigEndian : ns) {
            longToBigEndian(longToBigEndian, bs, off);
            off += 8;
        }
    }

    public static int littleEndianToInt(byte[] bs, int off) {
        off++;
        off++;
        return (((bs[off] & 255) | ((bs[off] & 255) << 8)) | ((bs[off] & 255) << 16)) | (bs[off + 1] << 24);
    }

    public static void littleEndianToInt(byte[] bs, int off, int[] ns) {
        for (int i = 0; i < ns.length; i++) {
            ns[i] = littleEndianToInt(bs, off);
            off += 4;
        }
    }

    public static void littleEndianToInt(byte[] bs, int bOff, int[] ns, int nOff, int count) {
        for (int i = 0; i < count; i++) {
            ns[nOff + i] = littleEndianToInt(bs, bOff);
            bOff += 4;
        }
    }

    public static byte[] intToLittleEndian(int n) {
        byte[] bs = new byte[4];
        intToLittleEndian(n, bs, 0);
        return bs;
    }

    public static void intToLittleEndian(int n, byte[] bs, int off) {
        bs[off] = (byte) n;
        off++;
        bs[off] = (byte) (n >>> 8);
        off++;
        bs[off] = (byte) (n >>> 16);
        bs[off + 1] = (byte) (n >>> 24);
    }

    public static byte[] intToLittleEndian(int[] ns) {
        byte[] bs = new byte[(ns.length * 4)];
        intToLittleEndian(ns, bs, 0);
        return bs;
    }

    public static void intToLittleEndian(int[] ns, byte[] bs, int off) {
        for (int intToLittleEndian : ns) {
            intToLittleEndian(intToLittleEndian, bs, off);
            off += 4;
        }
    }

    public static long littleEndianToLong(byte[] bs, int off) {
        return ((((long) littleEndianToInt(bs, off + 4)) & 4294967295L) << 32) | (((long) littleEndianToInt(bs, off)) & 4294967295L);
    }

    public static void littleEndianToLong(byte[] bs, int off, long[] ns) {
        for (int i = 0; i < ns.length; i++) {
            ns[i] = littleEndianToLong(bs, off);
            off += 8;
        }
    }

    public static byte[] longToLittleEndian(long n) {
        byte[] bs = new byte[8];
        longToLittleEndian(n, bs, 0);
        return bs;
    }

    public static void longToLittleEndian(long n, byte[] bs, int off) {
        intToLittleEndian((int) (4294967295L & n), bs, off);
        intToLittleEndian((int) (n >>> 32), bs, off + 4);
    }

    public static byte[] longToLittleEndian(long[] ns) {
        byte[] bs = new byte[(ns.length * 8)];
        longToLittleEndian(ns, bs, 0);
        return bs;
    }

    public static void longToLittleEndian(long[] ns, byte[] bs, int off) {
        for (long longToLittleEndian : ns) {
            longToLittleEndian(longToLittleEndian, bs, off);
            off += 8;
        }
    }
}
