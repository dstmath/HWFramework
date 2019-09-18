package org.bouncycastle.pqc.crypto.newhope;

import java.security.SecureRandom;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.crypto.ExchangePair;
import org.bouncycastle.pqc.crypto.ExchangePairGenerator;

public class NHExchangePairGenerator implements ExchangePairGenerator {
    private final SecureRandom random;

    public NHExchangePairGenerator(SecureRandom secureRandom) {
        this.random = secureRandom;
    }

    public ExchangePair GenerateExchange(AsymmetricKeyParameter asymmetricKeyParameter) {
        return generateExchange(asymmetricKeyParameter);
    }

    public ExchangePair generateExchange(AsymmetricKeyParameter asymmetricKeyParameter) {
        byte[] bArr = new byte[32];
        byte[] bArr2 = new byte[2048];
        NewHope.sharedB(this.random, bArr, bArr2, ((NHPublicKeyParameters) asymmetricKeyParameter).pubData);
        return new ExchangePair(new NHPublicKeyParameters(bArr2), bArr);
    }
}
