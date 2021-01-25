package org.bouncycastle.crypto.modes.kgcm;

public class BasicKGCMMultiplier_256 implements KGCMMultiplier {
    private final long[] H = new long[4];

    @Override // org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier
    public void init(long[] jArr) {
        KGCMUtil_256.copy(jArr, this.H);
    }

    @Override // org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier
    public void multiplyH(long[] jArr) {
        KGCMUtil_256.multiply(jArr, this.H, jArr);
    }
}
