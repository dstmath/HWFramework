package org.bouncycastle.crypto.modes.kgcm;

public class BasicKGCMMultiplier_128 implements KGCMMultiplier {
    private final long[] H = new long[2];

    @Override // org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier
    public void init(long[] jArr) {
        KGCMUtil_128.copy(jArr, this.H);
    }

    @Override // org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier
    public void multiplyH(long[] jArr) {
        KGCMUtil_128.multiply(jArr, this.H, jArr);
    }
}
