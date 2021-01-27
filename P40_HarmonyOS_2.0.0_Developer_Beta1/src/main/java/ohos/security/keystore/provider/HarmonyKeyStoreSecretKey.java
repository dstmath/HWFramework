package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreKey;
import android.security.keystore.AndroidKeyStoreSecretKey;
import javax.crypto.SecretKey;

public class HarmonyKeyStoreSecretKey extends HarmonyKeyStoreKey implements SecretKey {
    private static final long serialVersionUID = 682040119817806087L;

    public HarmonyKeyStoreSecretKey(String str, int i, String str2) {
        super(str, i, str2);
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStoreKey
    public AndroidKeyStoreKey toAndroidKey() {
        return new AndroidKeyStoreSecretKey(getAlias(), getUid(), getAlgorithm());
    }
}
