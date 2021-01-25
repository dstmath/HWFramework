package com.huawei.security.keystore;

import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;

public class HwUniversalKeyStoreSM2PublicKey extends HwUniversalKeyStorePublicKey implements ECPublicKey {
    private static final boolean IS_FROM_GM = true;
    private static final long serialVersionUID = 1;
    private final transient ECParameterSpec mParams;
    private final transient ECPoint mW;

    public HwUniversalKeyStoreSM2PublicKey(String alias, int uid, byte[] x509EncodedForm, ECParameterSpec params, ECPoint w) {
        super(alias, uid, HwKeyProperties.KEY_ALGORITHM_EC, x509EncodedForm);
        this.mParams = params;
        this.mW = w;
    }

    public HwUniversalKeyStoreSM2PublicKey(String alias, int uid, ECPublicKey info) {
        this(alias, uid, info.getEncoded(), info.getParams(), info.getW());
        if (!"X.509".equalsIgnoreCase(info.getFormat())) {
            throw new IllegalArgumentException("Unsupported key export format: " + info.getFormat());
        }
    }

    @Override // java.security.interfaces.ECKey
    public ECParameterSpec getParams() {
        return this.mParams;
    }

    @Override // java.security.interfaces.ECPublicKey
    public ECPoint getW() {
        return this.mW;
    }

    @Override // com.huawei.security.keystore.HwUniversalKeyStorePublicKey, com.huawei.security.keystore.HwUniversalKeyStoreKey, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.security.keystore.HwUniversalKeyStorePublicKey, com.huawei.security.keystore.HwUniversalKeyStoreKey, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }

    public boolean getIsFromGm() {
        return IS_FROM_GM;
    }
}
