package org.bouncycastle.pqc.math.linearalgebra;

public final class CharUtils {
    private CharUtils() {
    }

    public static char[] clone(char[] cArr) {
        char[] cArr2 = new char[cArr.length];
        System.arraycopy(cArr, 0, cArr2, 0, cArr.length);
        return cArr2;
    }

    public static boolean equals(char[] cArr, char[] cArr2) {
        if (cArr.length != cArr2.length) {
            return false;
        }
        boolean z = true;
        for (int length = cArr.length - 1; length >= 0; length--) {
            z &= cArr[length] == cArr2[length];
        }
        return z;
    }

    public static byte[] toByteArray(char[] cArr) {
        byte[] bArr = new byte[cArr.length];
        for (int length = cArr.length - 1; length >= 0; length--) {
            bArr[length] = (byte) cArr[length];
        }
        return bArr;
    }

    public static byte[] toByteArrayForPBE(char[] cArr) {
        byte[] bArr = new byte[cArr.length];
        for (int i = 0; i < cArr.length; i++) {
            bArr[i] = (byte) cArr[i];
        }
        int length = bArr.length * 2;
        byte[] bArr2 = new byte[(length + 2)];
        for (int i2 = 0; i2 < bArr.length; i2++) {
            int i3 = i2 * 2;
            bArr2[i3] = 0;
            bArr2[i3 + 1] = bArr[i2];
        }
        bArr2[length] = 0;
        bArr2[length + 1] = 0;
        return bArr2;
    }
}
