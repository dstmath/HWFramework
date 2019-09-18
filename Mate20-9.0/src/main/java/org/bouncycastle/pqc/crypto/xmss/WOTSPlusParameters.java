package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.crypto.Digest;

final class WOTSPlusParameters {
    private final Digest digest;
    private final int digestSize;
    private final int len;
    private final int len1;
    private final int len2;
    private final XMSSOid oid;
    private final int winternitzParameter;

    protected WOTSPlusParameters(Digest digest2) {
        if (digest2 != null) {
            this.digest = digest2;
            this.digestSize = XMSSUtil.getDigestSize(digest2);
            this.winternitzParameter = 16;
            this.len1 = (int) Math.ceil(((double) (8 * this.digestSize)) / ((double) XMSSUtil.log2(this.winternitzParameter)));
            this.len2 = ((int) Math.floor((double) (XMSSUtil.log2(this.len1 * (this.winternitzParameter - 1)) / XMSSUtil.log2(this.winternitzParameter)))) + 1;
            this.len = this.len1 + this.len2;
            this.oid = WOTSPlusOid.lookup(digest2.getAlgorithmName(), this.digestSize, this.winternitzParameter, this.len);
            if (this.oid == null) {
                throw new IllegalArgumentException("cannot find OID for digest algorithm: " + digest2.getAlgorithmName());
            }
            return;
        }
        throw new NullPointerException("digest == null");
    }

    /* access modifiers changed from: protected */
    public Digest getDigest() {
        return this.digest;
    }

    /* access modifiers changed from: protected */
    public int getDigestSize() {
        return this.digestSize;
    }

    /* access modifiers changed from: protected */
    public int getLen() {
        return this.len;
    }

    /* access modifiers changed from: protected */
    public int getLen1() {
        return this.len1;
    }

    /* access modifiers changed from: protected */
    public int getLen2() {
        return this.len2;
    }

    /* access modifiers changed from: protected */
    public XMSSOid getOid() {
        return this.oid;
    }

    /* access modifiers changed from: protected */
    public int getWinternitzParameter() {
        return this.winternitzParameter;
    }
}
