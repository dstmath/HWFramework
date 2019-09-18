package org.bouncycastle.crypto.modes.kgcm;

import java.lang.reflect.Array;

public class Tables4kKGCMMultiplier_128 implements KGCMMultiplier {
    private long[][] T;

    public void init(long[] jArr) {
        if (this.T == null) {
            this.T = (long[][]) Array.newInstance(long.class, new int[]{256, 2});
        } else if (KGCMUtil_128.equal(jArr, this.T[1])) {
            return;
        }
        KGCMUtil_128.copy(jArr, this.T[1]);
        for (int i = 2; i < 256; i += 2) {
            KGCMUtil_128.multiplyX(this.T[i >> 1], this.T[i]);
            KGCMUtil_128.add(this.T[i], this.T[1], this.T[i + 1]);
        }
    }

    public void multiplyH(long[] jArr) {
        long[] jArr2 = new long[2];
        KGCMUtil_128.copy(this.T[((int) (jArr[1] >>> 56)) & 255], jArr2);
        for (int i = 14; i >= 0; i--) {
            KGCMUtil_128.multiplyX8(jArr2, jArr2);
            KGCMUtil_128.add(this.T[((int) (jArr[i >>> 3] >>> ((i & 7) << 3))) & 255], jArr2, jArr2);
        }
        KGCMUtil_128.copy(jArr2, jArr);
    }
}
