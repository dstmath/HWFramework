package com.huawei.security.keystore;

import java.security.PrivateKey;

public class HwUniversalKeyStorePrivateKey extends HwUniversalKeyStoreKey implements PrivateKey {
    private static final long serialVersionUID = 1;

    public HwUniversalKeyStorePrivateKey(String alias, int uid, String algorithm) {
        super(alias, uid, algorithm);
    }
}
