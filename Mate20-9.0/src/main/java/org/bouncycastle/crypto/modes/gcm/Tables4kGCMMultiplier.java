package org.bouncycastle.crypto.modes.gcm;

import java.lang.reflect.Array;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class Tables4kGCMMultiplier implements GCMMultiplier {
    private byte[] H;
    private long[][] T;

    public void init(byte[] bArr) {
        if (this.T == null) {
            this.T = (long[][]) Array.newInstance(long.class, new int[]{256, 2});
        } else if (Arrays.areEqual(this.H, bArr)) {
            return;
        }
        this.H = Arrays.clone(bArr);
        GCMUtil.asLongs(this.H, this.T[1]);
        GCMUtil.multiplyP7(this.T[1], this.T[1]);
        for (int i = 2; i < 256; i += 2) {
            GCMUtil.divideP(this.T[i >> 1], this.T[i]);
            GCMUtil.xor(this.T[i], this.T[1], this.T[i + 1]);
        }
    }

    public void multiplyH(byte[] bArr) {
        byte[] bArr2 = bArr;
        long[] jArr = this.T[bArr2[15] & 255];
        long j = jArr[0];
        long j2 = jArr[1];
        for (int i = 14; i >= 0; i--) {
            long[] jArr2 = this.T[bArr2[i] & 255];
            long j3 = j2 << 56;
            j2 = ((j2 >>> 8) | (j << 56)) ^ jArr2[1];
            j = (((((j >>> 8) ^ jArr2[0]) ^ j3) ^ (j3 >>> 1)) ^ (j3 >>> 2)) ^ (j3 >>> 7);
        }
        Pack.longToBigEndian(j, bArr2, 0);
        Pack.longToBigEndian(j2, bArr2, 8);
    }
}
