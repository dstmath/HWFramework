package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.AsymmetricBlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.crypto.params.RSAKeyParameters;
import com.android.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import com.android.org.bouncycastle.util.BigIntegers;
import java.math.BigInteger;
import java.security.SecureRandom;

public class RSABlindedEngine implements AsymmetricBlockCipher {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private RSACoreEngine core = new RSACoreEngine();
    private RSAKeyParameters key;
    private SecureRandom random;

    public void init(boolean forEncryption, CipherParameters param) {
        this.core.init(forEncryption, param);
        if (param instanceof ParametersWithRandom) {
            ParametersWithRandom rParam = (ParametersWithRandom) param;
            this.key = (RSAKeyParameters) rParam.getParameters();
            this.random = rParam.getRandom();
            return;
        }
        this.key = (RSAKeyParameters) param;
        this.random = new SecureRandom();
    }

    public int getInputBlockSize() {
        return this.core.getInputBlockSize();
    }

    public int getOutputBlockSize() {
        return this.core.getOutputBlockSize();
    }

    public byte[] processBlock(byte[] in, int inOff, int inLen) {
        BigInteger result;
        BigInteger result2;
        if (this.key != null) {
            BigInteger input = this.core.convertInput(in, inOff, inLen);
            if (this.key instanceof RSAPrivateCrtKeyParameters) {
                RSAPrivateCrtKeyParameters k = (RSAPrivateCrtKeyParameters) this.key;
                BigInteger e = k.getPublicExponent();
                if (e != null) {
                    BigInteger m = k.getModulus();
                    BigInteger r = BigIntegers.createRandomInRange(ONE, m.subtract(ONE), this.random);
                    result2 = this.core.processBlock(r.modPow(e, m).multiply(input).mod(m)).multiply(r.modInverse(m)).mod(m);
                    if (!input.equals(result2.modPow(e, m))) {
                        throw new IllegalStateException("RSA engine faulty decryption/signing detected");
                    }
                } else {
                    result2 = this.core.processBlock(input);
                }
                result = result2;
            } else {
                result = this.core.processBlock(input);
            }
            return this.core.convertOutput(result);
        }
        throw new IllegalStateException("RSA engine not initialised");
    }
}
