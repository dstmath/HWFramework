package org.bouncycastle.pqc.crypto.lms;

/* access modifiers changed from: package-private */
public class LMOtsPrivateKey {
    private final byte[] I;
    private final byte[] masterSecret;
    private final LMOtsParameters parameter;
    private final int q;

    public LMOtsPrivateKey(LMOtsParameters lMOtsParameters, byte[] bArr, int i, byte[] bArr2) {
        this.parameter = lMOtsParameters;
        this.I = bArr;
        this.q = i;
        this.masterSecret = bArr2;
    }

    /* access modifiers changed from: package-private */
    public SeedDerive getDerivationFunction() {
        SeedDerive seedDerive = new SeedDerive(this.I, this.masterSecret, DigestUtil.getDigest(this.parameter.getDigestOID()));
        seedDerive.setQ(this.q);
        return seedDerive;
    }

    public byte[] getI() {
        return this.I;
    }

    public byte[] getMasterSecret() {
        return this.masterSecret;
    }

    public LMOtsParameters getParameter() {
        return this.parameter;
    }

    public int getQ() {
        return this.q;
    }
}
