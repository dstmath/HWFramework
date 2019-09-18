package com.huawei.security.keystore;

import java.security.PublicKey;
import java.util.Arrays;

public class HwUniversalKeyStorePublicKey extends HwUniversalKeyStoreKey implements PublicKey {
    private static final long serialVersionUID = 1;
    private final byte[] mEncoded;

    public HwUniversalKeyStorePublicKey(String alias, int uid, String algorithm, byte[] x509EncodedForm) {
        super(alias, uid, algorithm);
        this.mEncoded = x509EncodedForm;
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] getEncoded() {
        return this.mEncoded;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (super.equals(obj) && getClass() == obj.getClass() && Arrays.equals(this.mEncoded, ((HwUniversalKeyStorePublicKey) obj).mEncoded)) {
            return true;
        }
        return false;
    }
}
