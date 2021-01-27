package android.security.keystore;

import javax.crypto.SecretKey;

public class AndroidKeyStoreSecretKey extends AndroidKeyStoreKey implements SecretKey {
    public AndroidKeyStoreSecretKey(String alias, int uid, String algorithm) {
        super(alias, uid, algorithm);
    }
}
