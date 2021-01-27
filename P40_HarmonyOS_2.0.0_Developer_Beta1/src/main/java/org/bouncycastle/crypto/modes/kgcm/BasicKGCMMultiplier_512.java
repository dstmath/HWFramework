package org.bouncycastle.crypto.modes.kgcm;

public class BasicKGCMMultiplier_512 implements KGCMMultiplier {
    private final long[] H = new long[8];

    @Override // org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier
    public void init(long[] jArr) {
        KGCMUtil_512.copy(jArr, this.H);
    }

    @Override // org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier
    public void multiplyH(long[] jArr) {
        KGCMUtil_512.multiply(jArr, this.H, jArr);
    }
}
