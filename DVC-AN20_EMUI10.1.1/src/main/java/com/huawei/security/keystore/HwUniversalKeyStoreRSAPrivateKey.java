package com.huawei.security.keystore;

import java.math.BigInteger;
import java.security.interfaces.RSAKey;

public class HwUniversalKeyStoreRSAPrivateKey extends HwUniversalKeyStorePrivateKey implements RSAKey {
    private static final long serialVersionUID = 1;
    private final BigInteger mModulus;

    public HwUniversalKeyStoreRSAPrivateKey(String alias, int uid, BigInteger modulus) {
        super(alias, uid, HwKeyProperties.KEY_ALGORITHM_RSA);
        this.mModulus = modulus;
    }

    public BigInteger getModulus() {
        return this.mModulus;
    }
}
