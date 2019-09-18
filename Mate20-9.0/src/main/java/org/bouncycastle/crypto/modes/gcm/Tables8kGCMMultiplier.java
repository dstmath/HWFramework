package org.bouncycastle.crypto.modes.gcm;

import java.lang.reflect.Array;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class Tables8kGCMMultiplier implements GCMMultiplier {
    private byte[] H;
    private long[][][] T;

    public void init(byte[] bArr) {
        if (this.T == null) {
            this.T = (long[][][]) Array.newInstance(long.class, new int[]{32, 16, 2});
        } else if (Arrays.areEqual(this.H, bArr)) {
            return;
        }
        this.H = Arrays.clone(bArr);
        for (int i = 0; i < 32; i++) {
            long[][] jArr = this.T[i];
            if (i == 0) {
                GCMUtil.asLongs(this.H, jArr[1]);
                GCMUtil.multiplyP3(jArr[1], jArr[1]);
            } else {
                GCMUtil.multiplyP4(this.T[i - 1][1], jArr[1]);
            }
            for (int i2 = 2; i2 < 16; i2 += 2) {
                GCMUtil.divideP(jArr[i2 >> 1], jArr[i2]);
                GCMUtil.xor(jArr[i2], jArr[1], jArr[i2 + 1]);
            }
        }
    }

    public void multiplyH(byte[] bArr) {
        long j = 0;
        long j2 = 0;
        for (int i = 15; i >= 0; i--) {
            int i2 = i + i;
            long[] jArr = this.T[i2 + 1][bArr[i] & 15];
            long[] jArr2 = this.T[i2][(bArr[i] & 240) >>> 4];
            j2 ^= jArr[0] ^ jArr2[0];
            j ^= jArr2[1] ^ jArr[1];
        }
        Pack.longToBigEndian(j2, bArr, 0);
        Pack.longToBigEndian(j, bArr, 8);
    }
}
