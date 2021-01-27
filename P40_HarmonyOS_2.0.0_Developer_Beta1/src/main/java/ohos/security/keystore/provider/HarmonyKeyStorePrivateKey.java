package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStorePrivateKey;
import java.security.PrivateKey;

public class HarmonyKeyStorePrivateKey extends HarmonyKeyStoreKey implements PrivateKey {
    private static final long serialVersionUID = 8046896903703995585L;

    public HarmonyKeyStorePrivateKey(String str, int i, String str2) {
        super(str, i, str2);
    }

    public PrivateKey toAndroidPrivateKey() {
        return new AndroidKeyStorePrivateKey(getAlias(), getUid(), getAlgorithm());
    }
}
