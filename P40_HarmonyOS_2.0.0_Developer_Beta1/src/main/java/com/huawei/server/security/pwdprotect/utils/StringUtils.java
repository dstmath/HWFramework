package com.huawei.server.security.pwdprotect.utils;

import java.security.SecureRandom;

public class StringUtils {
    private static final int BYTE_FIRST_INDEX = 2;
    private static final int IV_LENGTH = 16;

    public static byte[] concatByteArrays(byte[]... byteArrays) {
        if (byteArrays == null || byteArrays.length == 0) {
            return new byte[0];
        }
        if (byteArrays.length == 1) {
            return byteArrays[0];
        }
        byte[] mergedArrays = concat(byteArrays[0], byteArrays[1]);
        int byteArraysLength = byteArrays.length;
        for (int i = 2; i < byteArraysLength; i++) {
            mergedArrays = concat(mergedArrays, byteArrays[i]);
        }
        return mergedArrays;
    }

    public static byte[] createRandomIvBytes() {
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);
        return ivBytes;
    }

    private static byte[] concat(byte[] byteArrays, byte[] otherByteArrays) {
        if (byteArrays == null && otherByteArrays == null) {
            return new byte[0];
        }
        if (byteArrays == null) {
            return otherByteArrays;
        }
        if (otherByteArrays == null) {
            return byteArrays;
        }
        byte[] mergedByte = new byte[(byteArrays.length + otherByteArrays.length)];
        System.arraycopy(byteArrays, 0, mergedByte, 0, byteArrays.length);
        System.arraycopy(otherByteArrays, 0, mergedByte, byteArrays.length, otherByteArrays.length);
        return mergedByte;
    }
}
