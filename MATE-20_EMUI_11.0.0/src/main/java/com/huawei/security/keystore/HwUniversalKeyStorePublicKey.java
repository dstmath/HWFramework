package com.huawei.security.keystore;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.Arrays;

public class HwUniversalKeyStorePublicKey extends HwUniversalKeyStoreKey implements PublicKey {
    private static final String TAG = "HwUniversalKeyStorePublicKey";
    private static final long serialVersionUID = 1;
    private final byte[] mEncoded;

    public HwUniversalKeyStorePublicKey(String alias, int uid, String algorithm, byte[] x509EncodedForm) {
        super(alias, uid, algorithm);
        this.mEncoded = x509EncodedForm;
    }

    @Override // com.huawei.security.keystore.HwUniversalKeyStoreKey, java.security.Key
    public String getFormat() {
        return "X.509";
    }

    @Override // com.huawei.security.keystore.HwUniversalKeyStoreKey, java.security.Key
    public byte[] getEncoded() {
        return this.mEncoded;
    }

    @Override // com.huawei.security.keystore.HwUniversalKeyStoreKey, java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (super.equals(obj) && getClass() == obj.getClass() && Arrays.equals(this.mEncoded, ((HwUniversalKeyStorePublicKey) obj).mEncoded)) {
            return true;
        }
        return false;
    }

    @Override // com.huawei.security.keystore.HwUniversalKeyStoreKey, java.lang.Object
    public int hashCode() {
        byte[] bArr = this.mEncoded;
        if (bArr == null) {
            return 0;
        }
        try {
            return (((1 * 31) + new String(bArr, "UTF-8").hashCode()) * 31) + 1;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Convert to encodeString failed!");
            return 1;
        }
    }
}
