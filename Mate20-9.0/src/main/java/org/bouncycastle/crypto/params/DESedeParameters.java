package org.bouncycastle.crypto.params;

public class DESedeParameters extends DESParameters {
    public static final int DES_EDE_KEY_LENGTH = 24;

    public DESedeParameters(byte[] bArr) {
        super(bArr);
        if (isWeakKey(bArr, 0, bArr.length)) {
            throw new IllegalArgumentException("attempt to create weak DESede key");
        }
    }

    public static boolean isReal2Key(byte[] bArr, int i) {
        boolean z = false;
        for (int i2 = i; i2 != i + 8; i2++) {
            if (bArr[i2] != bArr[i2 + 8]) {
                z = true;
            }
        }
        return z;
    }

    public static boolean isReal3Key(byte[] bArr, int i) {
        int i2 = i;
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        while (true) {
            boolean z4 = true;
            if (i2 == i + 8) {
                break;
            }
            int i3 = i2 + 8;
            z |= bArr[i2] != bArr[i3];
            int i4 = i2 + 16;
            z2 |= bArr[i2] != bArr[i4];
            if (bArr[i3] == bArr[i4]) {
                z4 = false;
            }
            z3 |= z4;
            i2++;
        }
        return z && z2 && z3;
    }

    public static boolean isRealEDEKey(byte[] bArr, int i) {
        return bArr.length == 16 ? isReal2Key(bArr, i) : isReal3Key(bArr, i);
    }

    public static boolean isWeakKey(byte[] bArr, int i) {
        return isWeakKey(bArr, i, bArr.length - i);
    }

    public static boolean isWeakKey(byte[] bArr, int i, int i2) {
        while (i < i2) {
            if (DESParameters.isWeakKey(bArr, i)) {
                return true;
            }
            i += 8;
        }
        return false;
    }
}
