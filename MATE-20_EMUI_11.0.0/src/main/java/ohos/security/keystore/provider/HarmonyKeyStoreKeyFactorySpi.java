package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreKeyFactorySpi;
import android.security.keystore.KeyInfo;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import ohos.security.keystore.KeyStoreKeySpec;

public class HarmonyKeyStoreKeyFactorySpi extends AndroidKeyStoreKeyFactorySpi {
    /* access modifiers changed from: protected */
    public <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> cls) throws InvalidKeySpecException {
        if (KeyStoreKeySpec.class.equals(cls)) {
            return cls.cast(TransferUtils.convertKeySpec((KeyInfo) HarmonyKeyStoreKeyFactorySpi.super.engineGetKeySpec(TransferUtils.toAndroidKey(key), KeyInfo.class)));
        }
        throw new InvalidKeySpecException("Only KeyStoreKeySpec supported");
    }

    /* access modifiers changed from: protected */
    public Key engineTranslateKey(Key key) throws InvalidKeyException {
        return TransferUtils.toHarmonyKey(HarmonyKeyStoreKeyFactorySpi.super.engineTranslateKey(TransferUtils.toAndroidKey(key)));
    }
}
