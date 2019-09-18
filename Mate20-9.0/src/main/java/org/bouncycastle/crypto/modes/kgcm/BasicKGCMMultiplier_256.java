package org.bouncycastle.crypto.modes.kgcm;

public class BasicKGCMMultiplier_256 implements KGCMMultiplier {
    private final long[] H = new long[4];

    public void init(long[] jArr) {
        KGCMUtil_256.copy(jArr, this.H);
    }

    public void multiplyH(long[] jArr) {
        KGCMUtil_256.multiply(jArr, this.H, jArr);
    }
}
