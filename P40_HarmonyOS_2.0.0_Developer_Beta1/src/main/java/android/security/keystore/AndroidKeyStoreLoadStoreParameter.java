package android.security.keystore;

import java.security.KeyStore;

class AndroidKeyStoreLoadStoreParameter implements KeyStore.LoadStoreParameter {
    private final int mUid;

    AndroidKeyStoreLoadStoreParameter(int uid) {
        this.mUid = uid;
    }

    @Override // java.security.KeyStore.LoadStoreParameter
    public KeyStore.ProtectionParameter getProtectionParameter() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getUid() {
        return this.mUid;
    }
}
