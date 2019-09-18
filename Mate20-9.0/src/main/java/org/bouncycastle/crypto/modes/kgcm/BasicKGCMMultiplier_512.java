package org.bouncycastle.crypto.modes.kgcm;

public class BasicKGCMMultiplier_512 implements KGCMMultiplier {
    private final long[] H = new long[8];

    public void init(long[] jArr) {
        KGCMUtil_512.copy(jArr, this.H);
    }

    public void multiplyH(long[] jArr) {
        KGCMUtil_512.multiply(jArr, this.H, jArr);
    }
}
