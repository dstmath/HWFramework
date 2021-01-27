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

    @Override // org.bouncycastle.crypto.AsymmetricBlockCipher
    public int getInputBlockSize() {
        return this.core.getInputBlockSize();
    }

    @Override // org.bouncycastle.crypto.AsymmetricBlockCipher
    public int getOutputBlockSize() {
        return this.core.getOutputBlockSize();
    }

    @Override // org.bouncycastle.crypto.AsymmetricBlockCipher
    public void init(boolean z, CipherParameters cipherParameters) {
        SecureRandom secureRandom;
        this.core.init(z, cipherParameters);
        if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.key = (RSAKeyParameters) parametersWithRandom.getParameters();
            if (this.key instanceof RSAPrivateCrtKeyParameters) {
                secureRandom = parametersWithRandom.getRandom();
            }
            this.random = null;
            return;
        }
        this.key = (RSAKeyParameters) cipherParameters;
        if (this.key instanceof RSAPrivateCrtKeyParameters) {
            secureRandom = CryptoServicesRegistrar.getSecureRandom();
        }
        this.random = null;
        return;
        this.random = secureRandom;
    }

    @Override // org.bouncycastle.crypto.AsymmetricBlockCipher
    public byte[] processBlock(byte[] bArr, int i, int i2) {
        BigInteger bigInteger;
        RSAPrivateCrtKeyParameters rSAPrivateCrtKeyParameters;
        BigInteger publicExponent;
        if (this.key != null) {
            BigInteger convertInput = this.core.convertInput(bArr, i, i2);
            RSAKeyParameters rSAKeyParameters = this.key;
            if (!(rSAKeyParameters instanceof RSAPrivateCrtKeyParameters) || (publicExponent = (rSAPrivateCrtKeyParameters = (RSAPrivateCrtKeyParameters) rSAKeyParameters).getPublicExponent()) == null) {
                bigInteger = this.core.processBlock(convertInput);
            } else {
                BigInteger modulus = rSAPrivateCrtKeyParameters.getModulus();
                BigInteger bigInteger2 = ONE;
                BigInteger createRandomInRange = BigIntegers.createRandomInRange(bigInteger2, modulus.subtract(bigInteger2), this.random);
                bigInteger = this.core.processBlock(createRandomInRange.modPow(publicExponent, modulus).multiply(convertInput).mod(modulus)).multiply(createRandomInRange.modInverse(modulus)).mod(modulus);
                if (!convertInput.equals(bigInteger.modPow(publicExponent, modulus))) {
                    throw new IllegalStateException("RSA engine faulty decryption/signing detected");
                }
            }
            return this.core.convertOutput(bigInteger);
        }
        throw new IllegalStateException("RSA engine not initialised");
    }
}
