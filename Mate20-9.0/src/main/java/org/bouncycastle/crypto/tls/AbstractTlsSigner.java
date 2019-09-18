package org.bouncycastle.crypto.tls;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public abstract class AbstractTlsSigner implements TlsSigner {
    protected TlsContext context;

    public Signer createSigner(AsymmetricKeyParameter asymmetricKeyParameter) {
        return createSigner(null, asymmetricKeyParameter);
    }

    public Signer createVerifyer(AsymmetricKeyParameter asymmetricKeyParameter) {
        return createVerifyer(null, asymmetricKeyParameter);
    }

    public byte[] generateRawSignature(AsymmetricKeyParameter asymmetricKeyParameter, byte[] bArr) throws CryptoException {
        return generateRawSignature(null, asymmetricKeyParameter, bArr);
    }

    public void init(TlsContext tlsContext) {
        this.context = tlsContext;
    }

    public boolean verifyRawSignature(byte[] bArr, AsymmetricKeyParameter asymmetricKeyParameter, byte[] bArr2) throws CryptoException {
        return verifyRawSignature(null, bArr, asymmetricKeyParameter, bArr2);
    }
}
