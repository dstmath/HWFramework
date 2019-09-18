package org.bouncycastle.crypto.modes.kgcm;

public class BasicKGCMMultiplier_128 implements KGCMMultiplier {
    private final long[] H = new long[2];

    public void init(long[] jArr) {
        KGCMUtil_128.copy(jArr, this.H);
    }

    public void multiplyH(long[] jArr) {
        KGCMUtil_128.multiply(jArr, this.H, jArr);
    }
}
