package org.bouncycastle.crypto.modes.gcm;

import java.lang.reflect.Array;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class Tables64kGCMMultiplier implements GCMMultiplier {
    private byte[] H;
    private long[][][] T;

    public void init(byte[] bArr) {
        if (this.T == null) {
            this.T = (long[][][]) Array.newInstance(long.class, new int[]{16, 256, 2});
        } else if (Arrays.areEqual(this.H, bArr)) {
            return;
        }
        this.H = Arrays.clone(bArr);
        for (int i = 0; i < 16; i++) {
            long[][] jArr = this.T[i];
            if (i == 0) {
                GCMUtil.asLongs(this.H, jArr[1]);
                GCMUtil.multiplyP7(jArr[1], jArr[1]);
            } else {
                GCMUtil.multiplyP8(this.T[i - 1][1], jArr[1]);
            }
            for (int i2 = 2; i2 < 256; i2 += 2) {
                GCMUtil.divideP(jArr[i2 >> 1], jArr[i2]);
                GCMUtil.xor(jArr[i2], jArr[1], jArr[i2 + 1]);
            }
        }
    }

    public void multiplyH(byte[] bArr) {
        long[] jArr = this.T[15][bArr[15] & 255];
        long j = jArr[0];
        long j2 = jArr[1];
        for (int i = 14; i >= 0; i--) {
            long[] jArr2 = this.T[i][bArr[i] & 255];
            j ^= jArr2[0];
            j2 ^= jArr2[1];
        }
        Pack.longToBigEndian(j, bArr, 0);
        Pack.longToBigEndian(j2, bArr, 8);
    }
}
