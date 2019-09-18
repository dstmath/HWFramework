package com.huawei.security.keystore;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;

public class HwUniversalKeyStoreRSAPublicKey extends HwUniversalKeyStorePublicKey implements RSAPublicKey {
    private static final long serialVersionUID = 1;
    private final BigInteger mModulus;
    private final BigInteger mPublicExponent;

    public HwUniversalKeyStoreRSAPublicKey(String alias, int uid, byte[] x509EncodedForm, BigInteger modulus, BigInteger publicExponent) {
        super(alias, uid, HwKeyProperties.KEY_ALGORITHM_RSA, x509EncodedForm);
        this.mModulus = modulus;
        this.mPublicExponent = publicExponent;
    }

    public HwUniversalKeyStoreRSAPublicKey(String alias, int uid, RSAPublicKey info) {
        this(alias, uid, info.getEncoded(), info.getModulus(), info.getPublicExponent());
        if (!"X.509".equalsIgnoreCase(info.getFormat())) {
            throw new IllegalArgumentException("Unsupported key export format: " + info.getFormat());
        }
    }

    public BigInteger getModulus() {
        return this.mModulus;
    }

    public BigInteger getPublicExponent() {
        return this.mPublicExponent;
    }
}
