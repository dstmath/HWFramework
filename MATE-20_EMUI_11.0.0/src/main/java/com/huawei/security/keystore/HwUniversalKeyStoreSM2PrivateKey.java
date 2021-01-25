package com.huawei.security.keystore;

import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;

public class HwUniversalKeyStoreSM2PrivateKey extends HwUniversalKeyStorePrivateKey implements ECKey {
    private static final boolean IS_FROM_GM = true;
    private static final long serialVersionUID = 1;
    private final transient ECParameterSpec mParams;

    public HwUniversalKeyStoreSM2PrivateKey(String alias, int uid, ECParameterSpec params) {
        super(alias, uid, HwKeyProperties.KEY_ALGORITHM_EC);
        this.mParams = params;
    }

    @Override // java.security.interfaces.ECKey
    public ECParameterSpec getParams() {
        return this.mParams;
    }

    public boolean getIsFromGm() {
        return IS_FROM_GM;
    }

    @Override // com.huawei.security.keystore.HwUniversalKeyStoreKey, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.security.keystore.HwUniversalKeyStoreKey, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
