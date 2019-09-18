package org.bouncycastle.pqc.crypto.gmss;

public class GMSSPublicKeyParameters extends GMSSKeyParameters {
    private byte[] gmssPublicKey;

    public GMSSPublicKeyParameters(byte[] bArr, GMSSParameters gMSSParameters) {
        super(false, gMSSParameters);
        this.gmssPublicKey = bArr;
    }

    public byte[] getPublicKey() {
        return this.gmssPublicKey;
    }
}
