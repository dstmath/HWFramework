package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.crypto.Digest;

/* access modifiers changed from: package-private */
public final class WOTSPlusParameters {
    private final int digestSize;
    private final int len;
    private final int len1;
    private final int len2;
    private final XMSSOid oid;
    private final ASN1ObjectIdentifier treeDigest;
    private final int winternitzParameter;

    protected WOTSPlusParameters(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        if (aSN1ObjectIdentifier != null) {
            this.treeDigest = aSN1ObjectIdentifier;
            Digest digest = DigestUtil.getDigest(aSN1ObjectIdentifier);
            this.digestSize = XMSSUtil.getDigestSize(digest);
            this.winternitzParameter = 16;
            this.len1 = (int) Math.ceil(((double) (this.digestSize * 8)) / ((double) XMSSUtil.log2(this.winternitzParameter)));
            this.len2 = ((int) Math.floor((double) (XMSSUtil.log2(this.len1 * (this.winternitzParameter - 1)) / XMSSUtil.log2(this.winternitzParameter)))) + 1;
            this.len = this.len1 + this.len2;
            this.oid = WOTSPlusOid.lookup(digest.getAlgorithmName(), this.digestSize, this.winternitzParameter, this.len);
            if (this.oid == null) {
                throw new IllegalArgumentException("cannot find OID for digest algorithm: " + digest.getAlgorithmName());
            }
            return;
        }
        throw new NullPointerException("treeDigest == null");
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

    public ASN1ObjectIdentifier getTreeDigest() {
        return this.treeDigest;
    }

    /* access modifiers changed from: protected */
    public int getTreeDigestSize() {
        return this.digestSize;
    }

    /* access modifiers changed from: protected */
    public int getWinternitzParameter() {
        return this.winternitzParameter;
    }
}
