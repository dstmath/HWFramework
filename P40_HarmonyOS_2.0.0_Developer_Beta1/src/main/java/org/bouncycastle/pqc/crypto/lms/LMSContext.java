package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.crypto.Digest;

public class LMSContext implements Digest {
    private final byte[] C;
    private volatile Digest digest;
    private final LMOtsPrivateKey key;
    private final byte[][] path;
    private final LMOtsPublicKey publicKey;
    private final LMSigParameters sigParams;
    private final Object signature;
    private LMSSignedPubKey[] signedPubKeys;

    public LMSContext(LMOtsPrivateKey lMOtsPrivateKey, LMSigParameters lMSigParameters, Digest digest2, byte[] bArr, byte[][] bArr2) {
        this.key = lMOtsPrivateKey;
        this.sigParams = lMSigParameters;
        this.digest = digest2;
        this.C = bArr;
        this.path = bArr2;
        this.publicKey = null;
        this.signature = null;
    }

    public LMSContext(LMOtsPublicKey lMOtsPublicKey, Object obj, Digest digest2) {
        this.publicKey = lMOtsPublicKey;
        this.signature = obj;
        this.digest = digest2;
        this.C = null;
        this.key = null;
        this.sigParams = null;
        this.path = null;
    }

    @Override // org.bouncycastle.crypto.Digest
    public int doFinal(byte[] bArr, int i) {
        return this.digest.doFinal(bArr, i);
    }

    @Override // org.bouncycastle.crypto.Digest
    public String getAlgorithmName() {
        return this.digest.getAlgorithmName();
    }

    /* access modifiers changed from: package-private */
    public byte[] getC() {
        return this.C;
    }

    @Override // org.bouncycastle.crypto.Digest
    public int getDigestSize() {
        return this.digest.getDigestSize();
    }

    /* access modifiers changed from: package-private */
    public byte[][] getPath() {
        return this.path;
    }

    /* access modifiers changed from: package-private */
    public LMOtsPrivateKey getPrivateKey() {
        return this.key;
    }

    public LMOtsPublicKey getPublicKey() {
        return this.publicKey;
    }

    /* access modifiers changed from: package-private */
    public byte[] getQ() {
        byte[] bArr = new byte[34];
        this.digest.doFinal(bArr, 0);
        this.digest = null;
        return bArr;
    }

    /* access modifiers changed from: package-private */
    public LMSigParameters getSigParams() {
        return this.sigParams;
    }

    public Object getSignature() {
        return this.signature;
    }

    /* access modifiers changed from: package-private */
    public LMSSignedPubKey[] getSignedPubKeys() {
        return this.signedPubKeys;
    }

    @Override // org.bouncycastle.crypto.Digest
    public void reset() {
        this.digest.reset();
    }

    @Override // org.bouncycastle.crypto.Digest
    public void update(byte b) {
        this.digest.update(b);
    }

    @Override // org.bouncycastle.crypto.Digest
    public void update(byte[] bArr, int i, int i2) {
        this.digest.update(bArr, i, i2);
    }

    /* access modifiers changed from: package-private */
    public LMSContext withSignedPublicKeys(LMSSignedPubKey[] lMSSignedPubKeyArr) {
        this.signedPubKeys = lMSSignedPubKeyArr;
        return this;
    }
}
