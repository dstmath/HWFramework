package org.bouncycastle.pqc.crypto.lms;

import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class LMSKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    LMSKeyGenerationParameters param;

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public AsymmetricCipherKeyPair generateKeyPair() {
        SecureRandom random = this.param.getRandom();
        byte[] bArr = new byte[16];
        random.nextBytes(bArr);
        byte[] bArr2 = new byte[32];
        random.nextBytes(bArr2);
        LMSPrivateKeyParameters generateKeys = LMS.generateKeys(this.param.getParameters().getLMSigParam(), this.param.getParameters().getLMOTSParam(), 0, bArr, bArr2);
        return new AsymmetricCipherKeyPair((AsymmetricKeyParameter) generateKeys.getPublicKey(), (AsymmetricKeyParameter) generateKeys);
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public void init(KeyGenerationParameters keyGenerationParameters) {
        this.param = (LMSKeyGenerationParameters) keyGenerationParameters;
    }
}
