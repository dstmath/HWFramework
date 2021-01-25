package org.bouncycastle.pqc.crypto.qtesla;

import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.MessageSigner;

public class QTESLASigner implements MessageSigner {
    private QTESLAPrivateKeyParameters privateKey;
    private QTESLAPublicKeyParameters publicKey;
    private SecureRandom secureRandom;

    @Override // org.bouncycastle.pqc.crypto.MessageSigner
    public byte[] generateSignature(byte[] bArr) {
        byte[] bArr2 = new byte[QTESLASecurityCategory.getSignatureSize(this.privateKey.getSecurityCategory())];
        int securityCategory = this.privateKey.getSecurityCategory();
        if (securityCategory == 5) {
            QTesla1p.generateSignature(bArr2, bArr, 0, bArr.length, this.privateKey.getSecret(), this.secureRandom);
        } else if (securityCategory == 6) {
            QTesla3p.generateSignature(bArr2, bArr, 0, bArr.length, this.privateKey.getSecret(), this.secureRandom);
        } else {
            throw new IllegalArgumentException("unknown security category: " + this.privateKey.getSecurityCategory());
        }
        return bArr2;
    }

    @Override // org.bouncycastle.pqc.crypto.MessageSigner
    public void init(boolean z, CipherParameters cipherParameters) {
        int i;
        if (z) {
            if (cipherParameters instanceof ParametersWithRandom) {
                ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
                this.secureRandom = parametersWithRandom.getRandom();
                this.privateKey = (QTESLAPrivateKeyParameters) parametersWithRandom.getParameters();
            } else {
                this.secureRandom = CryptoServicesRegistrar.getSecureRandom();
                this.privateKey = (QTESLAPrivateKeyParameters) cipherParameters;
            }
            this.publicKey = null;
            i = this.privateKey.getSecurityCategory();
        } else {
            this.privateKey = null;
            this.publicKey = (QTESLAPublicKeyParameters) cipherParameters;
            i = this.publicKey.getSecurityCategory();
        }
        QTESLASecurityCategory.validate(i);
    }

    @Override // org.bouncycastle.pqc.crypto.MessageSigner
    public boolean verifySignature(byte[] bArr, byte[] bArr2) {
        int i;
        int securityCategory = this.publicKey.getSecurityCategory();
        if (securityCategory == 5) {
            i = QTesla1p.verifying(bArr, bArr2, 0, bArr2.length, this.publicKey.getPublicData());
        } else if (securityCategory == 6) {
            i = QTesla3p.verifying(bArr, bArr2, 0, bArr2.length, this.publicKey.getPublicData());
        } else {
            throw new IllegalArgumentException("unknown security category: " + this.publicKey.getSecurityCategory());
        }
        return i == 0;
    }
}
