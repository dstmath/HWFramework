package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStorePublicKey;
import java.security.PublicKey;
import java.util.Arrays;

public class HarmonyKeyStorePublicKey extends HarmonyKeyStoreKey implements PublicKey {
    private static final long serialVersionUID = 1229108480662674911L;
    private final byte[] encodedBytes;

    @Override // ohos.security.keystore.provider.HarmonyKeyStoreKey, java.security.Key
    public String getFormat() {
        return "X.509";
    }

    public HarmonyKeyStorePublicKey(String str, int i, String str2, byte[] bArr) {
        super(str, i, str2);
        this.encodedBytes = ArrayUtils.cloneIfNotEmpty(bArr);
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStoreKey, java.security.Key
    public byte[] getEncoded() {
        return ArrayUtils.cloneIfNotEmpty(this.encodedBytes);
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStoreKey, java.lang.Object
    public int hashCode() {
        return (super.hashCode() * 31) + Arrays.hashCode(this.encodedBytes);
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStoreKey, java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (super.equals(obj) && getClass() == obj.getClass()) {
            return Arrays.equals(this.encodedBytes, ((HarmonyKeyStorePublicKey) obj).encodedBytes);
        }
        return false;
    }

    public PublicKey toAndroidPublicKey() {
        return new AndroidKeyStorePublicKey(getAlias(), getUid(), getAlgorithm(), this.encodedBytes);
    }
}
