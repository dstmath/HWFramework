package android.security.keystore;

import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.ProtectionParameter;

class AndroidKeyStoreLoadStoreParameter implements LoadStoreParameter {
    private final int mUid;

    AndroidKeyStoreLoadStoreParameter(int uid) {
        this.mUid = uid;
    }

    public ProtectionParameter getProtectionParameter() {
        return null;
    }

    int getUid() {
        return this.mUid;
    }
}
