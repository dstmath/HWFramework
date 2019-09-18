package com.android.server.utils;

import java.security.NoSuchAlgorithmException;

public class Calculator {
    public static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[(a.length < b.length ? a.length : b.length)];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    public static byte[] shiftLeft(byte[] data, int n) {
        byte[] rst = new byte[data.length];
        System.arraycopy(data, n, rst, 0, data.length - n);
        System.arraycopy(data, 0, rst, data.length - n, n);
        return rst;
    }

    public static byte[] shiftRight(byte[] data, int n) {
        byte[] rst = new byte[data.length];
        System.arraycopy(data, 0, rst, n, data.length - n);
        System.arraycopy(data, data.length - n, rst, 0, n);
        return rst;
    }

    public static byte[] calculator(byte[] c1, byte[] c2, byte[] c3) throws NoSuchAlgorithmException {
        return Sha.sha256(xor(shiftRight(Sha.sha256(xor(shiftLeft(c1, 4), c2)), 6), c3));
    }
}
