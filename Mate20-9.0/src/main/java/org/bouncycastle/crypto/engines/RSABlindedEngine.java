package org.bouncycastle.crypto.engines;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.util.BigIntegers;

public class RSABlindedEngine implements AsymmetricBlockCipher {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private RSACoreEngine core = new RSACoreEngine();
    private RSAKeyParameters key;
    private SecureRandom random;

    public int getInputBlockSize() {
        return this.core.getInputBlockSize();
    }

    public int getOutputBlockSize() {
        return this.core.getOutputBlockSize();
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        SecureRandom secureRandom;
        this.core.init(z, cipherParameters);
        if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.key = (RSAKeyParameters) parametersWithRandom.getParameters();
            secureRandom = parametersWithRandom.getRandom();
        } else {
            this.key = (RSAKeyParameters) cipherParameters;
            secureRandom = CryptoServicesRegistrar.getSecureRandom();
        }
        this.random = secureRandom;
    }

    public byte[] processBlock(byte[] bArr, int i, int i2) {
        BigInteger bigInteger;
        if (this.key != null) {
            BigInteger convertInput = this.core.convertInput(bArr, i, i2);
            if (this.key instanceof RSAPrivateCrtKeyParameters) {
                RSAPrivateCrtKeyParameters rSAPrivateCrtKeyParameters = (RSAPrivateCrtKeyParameters) this.key;
                BigInteger publicExponent = rSAPrivateCrtKeyParameters.getPublicExponent();
                if (publicExponent != null) {
                    BigInteger modulus = rSAPrivateCrtKeyParameters.getModulus();
                    BigInteger createRandomInRange = BigIntegers.createRandomInRange(ONE, modulus.subtract(ONE), this.random);
                    bigInteger = this.core.processBlock(createRandomInRange.modPow(publicExponent, modulus).multiply(convertInput).mod(modulus)).multiply(createRandomInRange.modInverse(modulus)).mod(modulus);
                    if (!convertInput.equals(bigInteger.modPow(publicExponent, modulus))) {
                        throw new IllegalStateException("RSA engine faulty decryption/signing detected");
                    }
                    return this.core.convertOutput(bigInteger);
                }
            }
            bigInteger = this.core.processBlock(convertInput);
            return this.core.convertOutput(bigInteger);
        }
        throw new IllegalStateException("RSA engine not initialised");
    }
}
