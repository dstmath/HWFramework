package org.bouncycastle.crypto.generators;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.Salsa20Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.Pack;

public class SCrypt {
    private SCrypt() {
    }

    private static void BlockMix(int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4, int i) {
        System.arraycopy(iArr, iArr.length - 16, iArr2, 0, 16);
        int length = iArr.length >>> 1;
        int i2 = 0;
        int i3 = 0;
        for (int i4 = i * 2; i4 > 0; i4--) {
            Xor(iArr2, iArr, i2, iArr3);
            Salsa20Engine.salsaCore(8, iArr3, iArr2);
            System.arraycopy(iArr2, 0, iArr4, i3, 16);
            i3 = (length + i2) - i3;
            i2 += 16;
        }
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
        for (int[] iArr2 : iArr) {
            Clear(iArr2);
        }
    }

    private static byte[] MFcrypt(byte[] bArr, byte[] bArr2, int i, int i2, int i3, int i4) {
        int i5 = i2 * 128;
        byte[] SingleIterationPBKDF2 = SingleIterationPBKDF2(bArr, bArr2, i3 * i5);
        int[] iArr = null;
        try {
            int length = SingleIterationPBKDF2.length >>> 2;
            iArr = new int[length];
            Pack.littleEndianToInt(SingleIterationPBKDF2, 0, iArr);
            int i6 = i * i2;
            int i7 = 0;
            while (i - i7 > 2 && i6 > 1024) {
                i7++;
                i6 >>>= 1;
            }
            int i8 = i5 >>> 2;
            for (int i9 = 0; i9 < length; i9 += i8) {
                SMix(iArr, i9, i, i7, i2);
            }
            Pack.intToLittleEndian(iArr, SingleIterationPBKDF2, 0);
            return SingleIterationPBKDF2(bArr, SingleIterationPBKDF2, i4);
        } finally {
            Clear(SingleIterationPBKDF2);
            Clear(iArr);
        }
    }

    private static void SMix(int[] iArr, int i, int i2, int i3, int i4) {
        int i5 = i2 >>> i3;
        int i6 = 1 << i3;
        int i7 = i5 - 1;
        int numberOfTrailingZeros = Integers.numberOfTrailingZeros(i2) - i3;
        int i8 = i4 * 32;
        int[] iArr2 = new int[16];
        int[] iArr3 = new int[16];
        int[] iArr4 = new int[i8];
        int[] iArr5 = new int[i8];
        int[][] iArr6 = new int[i6][];
        try {
            System.arraycopy(iArr, i, iArr5, 0, i8);
            int i9 = 0;
            while (i9 < i6) {
                int[] iArr7 = new int[(i5 * i8)];
                iArr6[i9] = iArr7;
                int i10 = 0;
                int i11 = 0;
                while (i11 < i5) {
                    System.arraycopy(iArr5, 0, iArr7, i10, i8);
                    int i12 = i10 + i8;
                    BlockMix(iArr5, iArr2, iArr3, iArr4, i4);
                    System.arraycopy(iArr4, 0, iArr7, i12, i8);
                    i10 = i12 + i8;
                    BlockMix(iArr4, iArr2, iArr3, iArr5, i4);
                    i11 += 2;
                    i5 = i5;
                }
                i9++;
                i6 = i6;
            }
            int i13 = i2 - 1;
            for (int i14 = 0; i14 < i2; i14++) {
                int i15 = iArr5[i8 - 16] & i13;
                System.arraycopy(iArr6[i15 >>> numberOfTrailingZeros], (i15 & i7) * i8, iArr4, 0, i8);
                Xor(iArr4, iArr5, 0, iArr4);
                BlockMix(iArr4, iArr2, iArr3, iArr5, i4);
            }
            System.arraycopy(iArr5, 0, iArr, i, i8);
            ClearAll(iArr6);
            ClearAll(new int[][]{iArr5, iArr2, iArr3, iArr4});
        } catch (Throwable th) {
            ClearAll(iArr6);
            ClearAll(new int[][]{iArr5, iArr2, iArr3, iArr4});
            throw th;
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
            int i5 = Integer.MAX_VALUE / ((i2 * 128) * 8);
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
