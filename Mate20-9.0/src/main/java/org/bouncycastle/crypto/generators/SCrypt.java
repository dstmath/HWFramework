package org.bouncycastle.crypto.generators;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.Salsa20Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class SCrypt {
    private SCrypt() {
    }

    private static void BlockMix(int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4, int i) {
        System.arraycopy(iArr, iArr.length - 16, iArr2, 0, 16);
        int length = iArr.length >>> 1;
        int i2 = 0;
        int i3 = 0;
        for (int i4 = 2 * i; i4 > 0; i4--) {
            Xor(iArr2, iArr, i2, iArr3);
            Salsa20Engine.salsaCore(8, iArr3, iArr2);
            System.arraycopy(iArr2, 0, iArr4, i3, 16);
            i3 = (length + i2) - i3;
            i2 += 16;
        }
        System.arraycopy(iArr4, 0, iArr, 0, iArr4.length);
    }

    private static void Clear(byte[] bArr) {
        if (bArr != null) {
            Arrays.fill(bArr, (byte) 0);
        }
    }

    private static void Clear(int[] iArr) {
        if (iArr != null) {
            Arrays.fill(iArr, 0);
        }
    }

    private static void ClearAll(int[][] iArr) {
        for (int[] Clear : iArr) {
            Clear(Clear);
        }
    }

    private static byte[] MFcrypt(byte[] bArr, byte[] bArr2, int i, int i2, int i3, int i4) {
        int[] iArr;
        int i5 = i2 * 128;
        byte[] SingleIterationPBKDF2 = SingleIterationPBKDF2(bArr, bArr2, i3 * i5);
        try {
            int length = SingleIterationPBKDF2.length >>> 2;
            iArr = new int[length];
            try {
                Pack.littleEndianToInt(SingleIterationPBKDF2, 0, iArr);
                int i6 = i5 >>> 2;
                for (int i7 = 0; i7 < length; i7 += i6) {
                    SMix(iArr, i7, i, i2);
                }
                Pack.intToLittleEndian(iArr, SingleIterationPBKDF2, 0);
                byte[] SingleIterationPBKDF22 = SingleIterationPBKDF2(bArr, SingleIterationPBKDF2, i4);
                Clear(SingleIterationPBKDF2);
                Clear(iArr);
                return SingleIterationPBKDF22;
            } catch (Throwable th) {
                th = th;
                Clear(SingleIterationPBKDF2);
                Clear(iArr);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            iArr = null;
            Clear(SingleIterationPBKDF2);
            Clear(iArr);
            throw th;
        }
    }

    private static void SMix(int[] iArr, int i, int i2, int i3) {
        int[] iArr2 = iArr;
        int i4 = i;
        int i5 = i2;
        int i6 = i3;
        int i7 = i6 * 32;
        int[] iArr3 = new int[16];
        int[] iArr4 = new int[16];
        int[] iArr5 = new int[i7];
        int[] iArr6 = new int[i7];
        int[][] iArr7 = new int[i5][];
        try {
            System.arraycopy(iArr2, i4, iArr6, 0, i7);
            for (int i8 = 0; i8 < i5; i8++) {
                iArr7[i8] = Arrays.clone(iArr6);
                BlockMix(iArr6, iArr3, iArr4, iArr5, i6);
            }
            int i9 = i5 - 1;
            for (int i10 = 0; i10 < i5; i10++) {
                Xor(iArr6, iArr7[iArr6[i7 - 16] & i9], 0, iArr6);
                BlockMix(iArr6, iArr3, iArr4, iArr5, i6);
            }
            System.arraycopy(iArr6, 0, iArr2, i4, i7);
        } finally {
            ClearAll(iArr7);
            ClearAll(new int[][]{iArr6, iArr3, iArr4, iArr5});
        }
    }

    private static byte[] SingleIterationPBKDF2(byte[] bArr, byte[] bArr2, int i) {
        PKCS5S2ParametersGenerator pKCS5S2ParametersGenerator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        pKCS5S2ParametersGenerator.init(bArr, bArr2, 1);
        return ((KeyParameter) pKCS5S2ParametersGenerator.generateDerivedMacParameters(i * 8)).getKey();
    }

    private static void Xor(int[] iArr, int[] iArr2, int i, int[] iArr3) {
        for (int length = iArr3.length - 1; length >= 0; length--) {
            iArr3[length] = iArr[length] ^ iArr2[i + length];
        }
    }

    public static byte[] generate(byte[] bArr, byte[] bArr2, int i, int i2, int i3, int i4) {
        if (bArr == null) {
            throw new IllegalArgumentException("Passphrase P must be provided.");
        } else if (bArr2 == null) {
            throw new IllegalArgumentException("Salt S must be provided.");
        } else if (i <= 1 || !isPowerOf2(i)) {
            throw new IllegalArgumentException("Cost parameter N must be > 1 and a power of 2");
        } else if (i2 == 1 && i >= 65536) {
            throw new IllegalArgumentException("Cost parameter N must be > 1 and < 65536.");
        } else if (i2 >= 1) {
            int i5 = Integer.MAX_VALUE / ((128 * i2) * 8);
            if (i3 < 1 || i3 > i5) {
                throw new IllegalArgumentException("Parallelisation parameter p must be >= 1 and <= " + i5 + " (based on block size r of " + i2 + ")");
            } else if (i4 >= 1) {
                return MFcrypt(bArr, bArr2, i, i2, i3, i4);
            } else {
                throw new IllegalArgumentException("Generated key length dkLen must be >= 1.");
            }
        } else {
            throw new IllegalArgumentException("Block size r must be >= 1.");
        }
    }

    private static boolean isPowerOf2(int i) {
        return (i & (i + -1)) == 0;
    }
}
