package org.bouncycastle.crypto.modes.gcm;

public class BasicGCMMultiplier implements GCMMultiplier {
    private long[] H;

    @Override // org.bouncycastle.crypto.modes.gcm.GCMMultiplier
    public void init(byte[] bArr) {
        this.H = GCMUtil.asLongs(bArr);
    }

    @Override // org.bouncycastle.crypto.modes.gcm.GCMMultiplier
    public void multiplyH(byte[] bArr) {
        long[] asLongs = GCMUtil.asLongs(bArr);
        GCMUtil.multiply(asLongs, this.H);
        GCMUtil.asBytes(asLongs, bArr);
    }
}
