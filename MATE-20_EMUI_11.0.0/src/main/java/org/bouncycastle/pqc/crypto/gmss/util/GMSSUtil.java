package org.bouncycastle.pqc.crypto.gmss.util;

import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;

public class GMSSUtil {
    public int bytesToIntLittleEndian(byte[] bArr) {
        return ((bArr[3] & 255) << 24) | (bArr[0] & 255) | ((bArr[1] & 255) << 8) | ((bArr[2] & 255) << 16);
    }

    public int bytesToIntLittleEndian(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        int i4 = (bArr[i] & 255) | ((bArr[i2] & 255) << 8);
        return ((bArr[i3 + 1] & 255) << 24) | i4 | ((bArr[i3] & 255) << 16);
    }

    public byte[] concatenateArray(byte[][] bArr) {
        byte[] bArr2 = new byte[(bArr.length * bArr[0].length)];
        int i = 0;
        for (int i2 = 0; i2 < bArr.length; i2++) {
            System.arraycopy(bArr[i2], 0, bArr2, i, bArr[i2].length);
            i += bArr[i2].length;
        }
        return bArr2;
    }

    public int getLog(int i) {
        int i2 = 1;
        int i3 = 2;
        while (i3 < i) {
            i3 <<= 1;
            i2++;
        }
        return i2;
    }

    public byte[] intToBytesLittleEndian(int i) {
        return new byte[]{(byte) (i & GF2Field.MASK), (byte) ((i >> 8) & GF2Field.MASK), (byte) ((i >> 16) & GF2Field.MASK), (byte) ((i >> 24) & GF2Field.MASK)};
    }

    public void printArray(String str, byte[] bArr) {
        System.out.println(str);
        int i = 0;
        for (int i2 = 0; i2 < bArr.length; i2++) {
            System.out.println(i + "; " + ((int) bArr[i2]));
            i++;
        }
    }

    public void printArray(String str, byte[][] bArr) {
        System.out.println(str);
        int i = 0;
        int i2 = 0;
        while (i < bArr.length) {
            int i3 = i2;
            for (int i4 = 0; i4 < bArr[0].length; i4++) {
                System.out.println(i3 + "; " + ((int) bArr[i][i4]));
                i3++;
            }
            i++;
            i2 = i3;
        }
    }

    public boolean testPowerOfTwo(int i) {
        int i2 = 1;
        while (i2 < i) {
            i2 <<= 1;
        }
        return i == i2;
    }
}
