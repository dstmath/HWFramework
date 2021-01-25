package org.bouncycastle.crypto.agreement.kdf;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.crypto.DerivationParameters;

public class DHKDFParameters implements DerivationParameters {
    private ASN1ObjectIdentifier algorithm;
    private byte[] extraInfo;
    private int keySize;
    private byte[] z;

    public DHKDFParameters(ASN1ObjectIdentifier aSN1ObjectIdentifier, int i, byte[] bArr) {
        this(aSN1ObjectIdentifier, i, bArr, null);
    }

    public DHKDFParameters(ASN1ObjectIdentifier aSN1ObjectIdentifier, int i, byte[] bArr, byte[] bArr2) {
        this.algorithm = aSN1ObjectIdentifier;
        this.keySize = i;
        this.z = bArr;
        this.extraInfo = bArr2;
    }

    public ASN1ObjectIdentifier getAlgorithm() {
        return this.algorithm;
    }

    public byte[] getExtraInfo() {
        return this.extraInfo;
    }

    public int getKeySize() {
        return this.keySize;
    }

    public byte[] getZ() {
        return this.z;
    }
}
