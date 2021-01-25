package com.huawei.security.keystore;

import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;

public class HwUniversalKeyStoreECPrivateKey extends HwUniversalKeyStorePrivateKey implements ECKey {
    private static final long serialVersionUID = -9158918763796969314L;
    private final ECParameterSpec mParams;

    public HwUniversalKeyStoreECPrivateKey(String alias, int uid, ECParameterSpec params) {
        super(alias, uid, HwKeyProperties.KEY_ALGORITHM_EC);
        this.mParams = params;
    }

    @Override // java.security.interfaces.ECKey
    public ECParameterSpec getParams() {
        return this.mParams;
    }
}
