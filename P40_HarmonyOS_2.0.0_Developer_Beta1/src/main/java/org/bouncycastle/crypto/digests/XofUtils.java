package org.bouncycastle.crypto.digests;

public class XofUtils {
    public static byte[] leftEncode(long j) {
        long j2 = j;
        byte b = 1;
        while (true) {
            j2 >>= 8;
            if (j2 == 0) {
                break;
            }
            b = (byte) (b + 1);
        }
        byte[] bArr = new byte[(b + 1)];
        bArr[0] = b;
        for (int i = 1; i <= b; i++) {
            bArr[i] = (byte) ((int) (j >> ((b - i) * 8)));
        }
        return bArr;
    }

    public static byte[] rightEncode(long j) {
        long j2 = j;
        byte b = 1;
        while (true) {
            j2 >>= 8;
            if (j2 == 0) {
                break;
            }
            b = (byte) (b + 1);
        }
        byte[] bArr = new byte[(b + 1)];
        bArr[b] = b;
        for (int i = 0; i < b; i++) {
            bArr[i] = (byte) ((int) (j >> (((b - i) - 1) * 8)));
        }
        return bArr;
    }
}
