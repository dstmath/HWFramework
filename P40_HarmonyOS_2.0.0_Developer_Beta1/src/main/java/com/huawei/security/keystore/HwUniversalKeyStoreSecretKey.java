package com.huawei.security.keystore;

import javax.crypto.SecretKey;

public class HwUniversalKeyStoreSecretKey extends HwUniversalKeyStoreKey implements SecretKey {
    private static final long serialVersionUID = 1;

    public HwUniversalKeyStoreSecretKey(String alias, int uid, String algorithm) {
        super(alias, uid, algorithm);
    }
}
