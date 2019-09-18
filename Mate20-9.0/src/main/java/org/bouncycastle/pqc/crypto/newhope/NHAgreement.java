package org.bouncycastle.pqc.crypto.newhope;

import org.bouncycastle.crypto.CipherParameters;

public class NHAgreement {
    private NHPrivateKeyParameters privKey;

    public byte[] calculateAgreement(CipherParameters cipherParameters) {
        byte[] bArr = new byte[32];
        NewHope.sharedA(bArr, this.privKey.secData, ((NHPublicKeyParameters) cipherParameters).pubData);
        return bArr;
    }

    public void init(CipherParameters cipherParameters) {
        this.privKey = (NHPrivateKeyParameters) cipherParameters;
    }
}
