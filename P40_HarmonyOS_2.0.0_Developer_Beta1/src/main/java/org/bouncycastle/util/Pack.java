package org.bouncycastle.util;

import org.bouncycastle.asn1.cmc.BodyPartID;

public abstract class Pack {
    public static int bigEndianToInt(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        return (bArr[i3 + 1] & 255) | (bArr[i] << 24) | ((bArr[i2] & 255) << 16) | ((bArr[i3] & 255) << 8);
    }

    public static void bigEndianToInt(byte[] bArr, int i, int[] iArr) {
        for (int i2 = 0; i2 < iArr.length; i2++) {
            iArr[i2] = bigEndianToInt(bArr, i);
            i += 4;
        }
    }

    public static long bigEndianToLong(byte[] bArr, int i) {
        int bigEndianToInt = bigEndianToInt(bArr, i);
        return (((long) bigEndianToInt(bArr, i + 4)) & BodyPartID.bodyIdMax) | ((((long) bigEndianToInt) & BodyPartID.bodyIdMax) << 32);
    }

    public static void bigEndianToLong(byte[] bArr, int i, long[] jArr) {
        for (int i2 = 0; i2 < jArr.length; i2++) {
            jArr[i2] = bigEndianToLong(bArr, i);
            i += 8;
        }
    }

    public static short bigEndianToShort(byte[] bArr, int i) {
        return (short) ((bArr[i + 1] & 255) | ((bArr[i] & 255) << 8));
    }

    public static void intToBigEndian(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) (i >>> 24);
        int i3 = i2 + 1;
        bArr[i3] = (byte) (i >>> 16);
        int i4 = i3 + 1;
        bArr[i4] = (byte) (i >>> 8);
        bArr[i4 + 1] = (byte) i;
    }

    public static void intToBigEndian(int[] iArr, byte[] bArr, int i) {
        for (int i2 : iArr) {
            intToBigEndian(i2, bArr, i);
            i += 4;
        }
    }

    public static byte[] intToBigEndian(int i) {
        byte[] bArr = new byte[4];
        intToBigEndian(i, bArr, 0);
        return bArr;
    }

    public static byte[] intToBigEndian(int[] iArr) {
        byte[] bArr = new byte[(iArr.length * 4)];
        intToBigEndian(iArr, bArr, 0);
        return bArr;
    }

    public static void intToLittleEndian(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) i;
        int i3 = i2 + 1;
        bArr[i3] = (byte) (i >>> 8);
        int i4 = i3 + 1;
        bArr[i4] = (byte) (i >>> 16);
        bArr[i4 + 1] = (byte) (i >>> 24);
    }

    public static void intToLittleEndian(int[] iArr, byte[] bArr, int i) {
        for (int i2 : iArr) {
            intToLittleEndian(i2, bArr, i);
            i += 4;
        }
    }

    public static byte[] intToLittleEndian(int i) {
        byte[] bArr = new byte[4];
        intToLittleEndian(i, bArr, 0);
        return bArr;
    }

    public static byte[] intToLittleEndian(int[] iArr) {
        byte[] bArr = new byte[(iArr.length * 4)];
        intToLittleEndian(iArr, bArr, 0);
        return bArr;
    }

    public static int littleEndianToInt(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        return (bArr[i3 + 1] << 24) | (bArr[i] & 255) | ((bArr[i2] & 255) << 8) | ((bArr[i3] & 255) << 16);
    }

    public static void littleEndianToInt(byte[] bArr, int i, int[] iArr) {
        for (int i2 = 0; i2 < iArr.length; i2++) {
            iArr[i2] = littleEndianToInt(bArr, i);
            i += 4;
        }
    }

    public static void littleEndianToInt(byte[] bArr, int i, int[] iArr, int i2, int i3) {
        for (int i4 = 0; i4 < i3; i4++) {
            iArr[i2 + i4] = littleEndianToInt(bArr, i);
            i += 4;
        }
    }

    public static int[] littleEndianToInt(byte[] bArr, int i, int i2) {
        int[] iArr = new int[i2];
        for (int i3 = 0; i3 < iArr.length; i3++) {
            iArr[i3] = littleEndianToInt(bArr, i);
            i += 4;
        }
        return iArr;
    }

    public static long littleEndianToLong(byte[] bArr, int i) {
        return ((((long) littleEndianToInt(bArr, i + 4)) & BodyPartID.bodyIdMax) << 32) | (((long) littleEndianToInt(bArr, i)) & BodyPartID.bodyIdMax);
    }

    public static void littleEndianToLong(byte[] bArr, int i, long[] jArr) {
        for (int i2 = 0; i2 < jArr.length; i2++) {
            jArr[i2] = littleEndianToLong(bArr, i);
            i += 8;
        }
    }

    public static void littleEndianToLong(byte[] bArr, int i, long[] jArr, int i2, int i3) {
        for (int i4 = 0; i4 < i3; i4++) {
            jArr[i2 + i4] = littleEndianToLong(bArr, i);
            i += 8;
        }
    }

    public static short littleEndianToShort(byte[] bArr, int i) {
        return (short) (((bArr[i + 1] & 255) << 8) | (bArr[i] & 255));
    }

    public static void longToBigEndian(long j, byte[] bArr, int i) {
        intToBigEndian((int) (j >>> 32), bArr, i);
        intToBigEndian((int) (j & BodyPartID.bodyIdMax), bArr, i + 4);
    }

    public static void longToBigEndian(long j, byte[] bArr, int i, int i2) {
        for (int i3 = i2 - 1; i3 >= 0; i3--) {
            bArr[i3 + i] = (byte) ((int) (255 & j));
            j >>>= 8;
        }
    }

    public static void longToBigEndian(long[] jArr, byte[] bArr, int i) {
        for (long j : jArr) {
            longToBigEndian(j, bArr, i);
            i += 8;
        }
    }

    public static byte[] longToBigEndian(long j) {
        byte[] bArr = new byte[8];
        longToBigEndian(j, bArr, 0);
        return bArr;
    }

    public static byte[] longToBigEndian(long[] jArr) {
        byte[] bArr = new byte[(jArr.length * 8)];
        longToBigEndian(jArr, bArr, 0);
        return bArr;
    }

    public static void longToLittleEndian(long j, byte[] bArr, int i) {
        intToLittleEndian((int) (BodyPartID.bodyIdMax & j), bArr, i);
        intToLittleEndian((int) (j >>> 32), bArr, i + 4);
    }

    public static void longToLittleEndian(long[] jArr, int i, int i2, byte[] bArr, int i3) {
        for (int i4 = 0; i4 < i2; i4++) {
            longToLittleEndian(jArr[i + i4], bArr, i3);
            i3 += 8;
        }
    }

    public static void longToLittleEndian(long[] jArr, byte[] bArr, int i) {
        for (long j : jArr) {
            longToLittleEndian(j, bArr, i);
            i += 8;
        }
    }

    public static byte[] longToLittleEndian(long j) {
        byte[] bArr = new byte[8];
        longToLittleEndian(j, bArr, 0);
        return bArr;
    }

    public static byte[] longToLittleEndian(long[] jArr) {
        byte[] bArr = new byte[(jArr.length * 8)];
        longToLittleEndian(jArr, bArr, 0);
        return bArr;
    }

    public static void shortToBigEndian(short s, byte[] bArr, int i) {
        bArr[i] = (byte) (s >>> 8);
        bArr[i + 1] = (byte) s;
    }

    public static byte[] shortToBigEndian(short s) {
        byte[] bArr = new byte[2];
        shortToBigEndian(s, bArr, 0);
        return bArr;
    }

    public static void shortToLittleEndian(short s, byte[] bArr, int i) {
        bArr[i] = (byte) s;
        bArr[i + 1] = (byte) (s >>> 8);
    }

    public static byte[] shortToLittleEndian(short s) {
        byte[] bArr = new byte[2];
        shortToLittleEndian(s, bArr, 0);
        return bArr;
    }
}
