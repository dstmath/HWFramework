package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreECPrivateKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.security.interfaces.ECKey;
import java.security.spec.ECParameterSpec;
import ohos.security.keystore.KeyStoreConstants;

public class HarmonyKeyStoreECPrivateKey extends HarmonyKeyStorePrivateKey implements ECKey {
    private static final long serialVersionUID = -9168164565576443366L;
    private final transient ECParameterSpec params;

    public HarmonyKeyStoreECPrivateKey(String str, int i, ECParameterSpec eCParameterSpec) {
        super(str, i, KeyStoreConstants.SEC_KEY_ALGORITHM_EC);
        this.params = eCParameterSpec;
    }

    @Override // java.security.interfaces.ECKey
    public ECParameterSpec getParams() {
        return this.params;
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStorePrivateKey
    public PrivateKey toAndroidPrivateKey() {
        return new AndroidKeyStoreECPrivateKey(getAlias(), getUid(), this.params);
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStoreKey, java.lang.Object
    public int hashCode() {
        int hashCode = super.hashCode() * 31;
        ECParameterSpec eCParameterSpec = this.params;
        return hashCode + (eCParameterSpec == null ? 0 : eCParameterSpec.hashCode());
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStoreKey, java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (super.equals(obj) && getClass() == obj.getClass()) {
            return this.params == ((HarmonyKeyStoreECPrivateKey) obj).params;
        }
        return false;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
    }
}
