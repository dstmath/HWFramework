package android.security.keystore;

import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;

public class AndroidKeyStoreECPrivateKey extends AndroidKeyStorePrivateKey implements ECKey {
    private final ECParameterSpec mParams;

    public AndroidKeyStoreECPrivateKey(String alias, int uid, ECParameterSpec params) {
        super(alias, uid, KeyProperties.KEY_ALGORITHM_EC);
        this.mParams = params;
    }

    @Override // java.security.interfaces.ECKey
    public ECParameterSpec getParams() {
        return this.mParams;
    }
}
