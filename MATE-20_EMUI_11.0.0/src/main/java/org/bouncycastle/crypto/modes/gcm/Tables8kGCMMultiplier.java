package org.bouncycastle.crypto.modes.gcm;

import java.lang.reflect.Array;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class Tables8kGCMMultiplier implements GCMMultiplier {
    private byte[] H;
    private long[][][] T;

    @Override // org.bouncycastle.crypto.modes.gcm.GCMMultiplier
    public void init(byte[] bArr) {
        if (this.T == null) {
            this.T = (long[][][]) Array.newInstance(long.class, 32, 16, 2);
        } else if (Arrays.areEqual(this.H, bArr)) {
            return;
        }
        this.H = Arrays.clone(bArr);
        for (int i = 0; i < 32; i++) {
            long[][][] jArr = this.T;
            long[][] jArr2 = jArr[i];
            if (i == 0) {
                GCMUtil.asLongs(this.H, jArr2[1]);
                GCMUtil.multiplyP3(jArr2[1], jArr2[1]);
            } else {
                GCMUtil.multiplyP4(jArr[i - 1][1], jArr2[1]);
            }
            for (int i2 = 2; i2 < 16; i2 += 2) {
                GCMUtil.divideP(jArr2[i2 >> 1], jArr2[i2]);
                GCMUtil.xor(jArr2[i2], jArr2[1], jArr2[i2 + 1]);
            }
        }
    }

    @Override // org.bouncycastle.crypto.modes.gcm.GCMMultiplier
    public void multiplyH(byte[] bArr) {
        long j = 0;
        long j2 = 0;
        for (int i = 15; i >= 0; i--) {
            long[][][] jArr = this.T;
            int i2 = i + i;
            long[] jArr2 = jArr[i2 + 1][bArr[i] & 15];
            long[] jArr3 = jArr[i2][(bArr[i] & 240) >>> 4];
            j2 ^= jArr2[0] ^ jArr3[0];
            j ^= jArr3[1] ^ jArr2[1];
        }
        Pack.longToBigEndian(j2, bArr, 0);
        Pack.longToBigEndian(j, bArr, 8);
    }
}
