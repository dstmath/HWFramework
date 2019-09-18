package org.bouncycastle.crypto.modes.kgcm;

import java.lang.reflect.Array;

public class Tables16kKGCMMultiplier_512 implements KGCMMultiplier {
    private long[][] T;

    public void init(long[] jArr) {
        if (this.T == null) {
            this.T = (long[][]) Array.newInstance(long.class, new int[]{256, 8});
        } else if (KGCMUtil_512.equal(jArr, this.T[1])) {
            return;
        }
        KGCMUtil_512.copy(jArr, this.T[1]);
        for (int i = 2; i < 256; i += 2) {
            KGCMUtil_512.multiplyX(this.T[i >> 1], this.T[i]);
            KGCMUtil_512.add(this.T[i], this.T[1], this.T[i + 1]);
        }
    }

    public void multiplyH(long[] jArr) {
        long[] jArr2 = new long[8];
        KGCMUtil_512.copy(this.T[((int) (jArr[7] >>> 56)) & 255], jArr2);
        for (int i = 62; i >= 0; i--) {
            KGCMUtil_512.multiplyX8(jArr2, jArr2);
            KGCMUtil_512.add(this.T[((int) (jArr[i >>> 3] >>> ((i & 7) << 3))) & 255], jArr2, jArr2);
        }
        KGCMUtil_512.copy(jArr2, jArr);
    }
}
