package com.android.server.security.pwdprotect.utils;

import java.security.SecureRandom;

public class StringUtils {
    public static byte[] byteMerger(byte[]... byteArrays) {
        byte[] tmpByteArrays = byteMergerTwoByte(byteArrays[0], byteArrays[1]);
        int byteArraysLength = byteArrays.length;
        for (int i = 2; i < byteArraysLength; i++) {
            tmpByteArrays = byteMergerTwoByte(tmpByteArrays, byteArrays[i]);
        }
        return tmpByteArrays;
    }

    private static byte[] byteMergerTwoByte(byte[] byteArrays, byte[] otherByteArrays) {
        byte[] tmpByte = new byte[(byteArrays.length + otherByteArrays.length)];
        System.arraycopy(byteArrays, 0, tmpByte, 0, byteArrays.length);
        System.arraycopy(otherByteArrays, 0, tmpByte, byteArrays.length, otherByteArrays.length);
        return tmpByte;
    }

    public static byte[] createRandomIvBytes() {
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);
        return ivBytes;
    }
}
