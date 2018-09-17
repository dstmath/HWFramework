package com.android.org.conscrypt;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

public class OpenSSLRSAKeyPairGenerator extends KeyPairGeneratorSpi {
    private int modulusBits = 2048;
    private byte[] publicExponent = new byte[]{(byte) 1, (byte) 0, (byte) 1};

    public KeyPair generateKeyPair() {
        OpenSSLKey key = new OpenSSLKey(NativeCrypto.RSA_generate_key_ex(this.modulusBits, this.publicExponent));
        return new KeyPair(new OpenSSLRSAPublicKey(key), OpenSSLRSAPrivateKey.getInstance(key));
    }

    public void initialize(int keysize, SecureRandom random) {
        this.modulusBits = keysize;
    }

    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof RSAKeyGenParameterSpec) {
            RSAKeyGenParameterSpec spec = (RSAKeyGenParameterSpec) params;
            BigInteger publicExponent = spec.getPublicExponent();
            if (publicExponent != null) {
                this.publicExponent = publicExponent.toByteArray();
            }
            this.modulusBits = spec.getKeysize();
            return;
        }
        throw new InvalidAlgorithmParameterException("Only RSAKeyGenParameterSpec supported");
    }
}
