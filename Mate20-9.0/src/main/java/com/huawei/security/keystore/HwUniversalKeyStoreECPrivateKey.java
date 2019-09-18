package com.huawei.security.keystore;

import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;

public class HwUniversalKeyStoreECPrivateKey extends HwUniversalKeyStorePrivateKey implements ECKey {
    private final ECParameterSpec mParams;

    public HwUniversalKeyStoreECPrivateKey(String alias, int uid, ECParameterSpec params) {
        super(alias, uid, HwKeyProperties.KEY_ALGORITHM_EC);
        this.mParams = params;
    }

    public ECParameterSpec getParams() {
        return this.mParams;
    }
}
