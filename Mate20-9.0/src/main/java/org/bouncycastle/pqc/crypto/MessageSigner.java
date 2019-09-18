package org.bouncycastle.pqc.crypto;

import org.bouncycastle.crypto.CipherParameters;

public interface MessageSigner {
    byte[] generateSignature(byte[] bArr);

    void init(boolean z, CipherParameters cipherParameters);

    boolean verifySignature(byte[] bArr, byte[] bArr2);
}
