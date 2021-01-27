package org.bouncycastle.pqc.crypto.newhope;

import org.bouncycastle.util.Arrays;

/* access modifiers changed from: package-private */
public class ErrorCorrection {
    ErrorCorrection() {
    }

    static short LDDecode(int i, int i2, int i3, int i4) {
        return (short) (((((g(i) + g(i2)) + g(i3)) + g(i4)) - 98312) >>> 31);
    }

    static int abs(int i) {
        int i2 = i >> 31;
        return (i ^ i2) - i2;
    }

    static int f(int[] iArr, int i, int i2, int i3) {
        int i4 = (i3 * 2730) >> 25;
        int i5 = i4 - ((12288 - (i3 - (i4 * 12289))) >> 31);
        iArr[i] = (i5 >> 1) + (i5 & 1);
        int i6 = i5 - 1;
        iArr[i2] = (i6 >> 1) + (i6 & 1);
        return abs(i3 - ((iArr[i] * 2) * 12289));
    }

    static int g(int i) {
        int i2 = (i * 2730) >> 27;
        int i3 = i2 - ((49155 - (i - (49156 * i2))) >> 31);
        return abs((((i3 >> 1) + (i3 & 1)) * 98312) - i);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0066: APUT  
      (r5v1 int[])
      (0 ??[int, short, byte, char])
      (wrap: int : 0x0064: ARITH  (r17v2 int) = (wrap: int : 0x005e: ARITH  (r17v1 int) = (r10v7 int) & (wrap: int : 0x005c: AGET  (r17v0 int) = (r1v1 int[]), (0 ??[int, short, byte, char]))) ^ (wrap: int : 0x0062: ARITH  (r18v1 int) = (r7v9 int) & (wrap: int : 0x0060: AGET  (r18v0 int) = (r1v1 int[]), (4 ??[int, float, short, byte, char]))))
     */
    static void helpRec(short[] sArr, short[] sArr2, byte[] bArr, byte b) {
        short s = 8;
        byte[] bArr2 = new byte[8];
        bArr2[0] = b;
        byte[] bArr3 = new byte[32];
        ChaCha20.process(bArr, bArr2, bArr3, 0, bArr3.length);
        int[] iArr = new int[8];
        int[] iArr2 = new int[4];
        int i = 0;
        while (i < 256) {
            int i2 = i + 0;
            int i3 = ((bArr3[i >>> 3] >>> (i & 7)) & 1) * 4;
            int i4 = i + 256;
            int i5 = i + 512;
            int i6 = i + 768;
            int f = (24577 - (((f(iArr, 0, 4, (sArr2[i2] * s) + i3) + f(iArr, 1, 5, (sArr2[i4] * s) + i3)) + f(iArr, 2, 6, (sArr2[i5] * s) + i3)) + f(iArr, 3, 7, (sArr2[i6] * 8) + i3))) >> 31;
            int i7 = ~f;
            iArr2[0] = (i7 & iArr[0]) ^ (f & iArr[4]);
            iArr2[1] = (i7 & iArr[1]) ^ (f & iArr[5]);
            iArr2[2] = (i7 & iArr[2]) ^ (f & iArr[6]);
            iArr2[3] = (iArr[7] & f) ^ (i7 & iArr[3]);
            sArr[i2] = (short) ((iArr2[0] - iArr2[3]) & 3);
            sArr[i4] = (short) ((iArr2[1] - iArr2[3]) & 3);
            sArr[i5] = (short) ((iArr2[2] - iArr2[3]) & 3);
            sArr[i6] = (short) (((-f) + (iArr2[3] * 2)) & 3);
            i++;
            s = 8;
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0022: APUT  
      (r1v1 int[])
      (0 ??[int, short, byte, char])
      (wrap: int : 0x0021: ARITH  (r4v3 int) = (wrap: int : 0x0015: ARITH  (r4v2 int) = (wrap: int : 0x0010: ARITH  (r4v1 int) = (wrap: short : 0x000e: AGET  (r4v0 short A[IMMUTABLE_TYPE]) = (r11v0 short[] A[IMMUTABLE_TYPE]), (r3v1 int)) * (8 short)) + (196624 int)) - (wrap: int : 0x001f: ARITH  (r3v5 int) = (wrap: int : 0x001e: ARITH  (r3v4 int) = (wrap: int : 0x0019: ARITH  (r3v3 int) = (wrap: short : 0x0016: AGET  (r3v2 short A[IMMUTABLE_TYPE]) = (r12v0 short[] A[IMMUTABLE_TYPE]), (r3v1 int)) * (2 short)) + (wrap: short : 0x001c: AGET  (r8v0 short A[IMMUTABLE_TYPE]) = (r12v0 short[] A[IMMUTABLE_TYPE]), (r7v0 int))) * (12289 int)))
     */
    static void rec(byte[] bArr, short[] sArr, short[] sArr2) {
        Arrays.fill(bArr, (byte) 0);
        int[] iArr = new int[4];
        for (int i = 0; i < 256; i++) {
            int i2 = i + 0;
            int i3 = i + 768;
            iArr[0] = ((sArr[i2] * 8) + 196624) - (((sArr2[i2] * 2) + sArr2[i3]) * 12289);
            int i4 = i + 256;
            iArr[1] = ((sArr[i4] * 8) + 196624) - (((sArr2[i4] * 2) + sArr2[i3]) * 12289);
            int i5 = i + 512;
            iArr[2] = ((sArr[i5] * 8) + 196624) - (((sArr2[i5] * 2) + sArr2[i3]) * 12289);
            iArr[3] = ((sArr[i3] * 8) + 196624) - (sArr2[i3] * 12289);
            int i6 = i >>> 3;
            bArr[i6] = (byte) ((LDDecode(iArr[0], iArr[1], iArr[2], iArr[3]) << (i & 7)) | bArr[i6]);
        }
    }
}
