package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreECPublicKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import ohos.security.keystore.KeyStoreConstants;

public class HarmonyKeyStoreECPublicKey extends HarmonyKeyStorePublicKey implements ECPublicKey {
    private static final long serialVersionUID = 1399735471133555768L;
    private final transient ECParameterSpec paramSpec;
    private final transient ECPoint point;

    public HarmonyKeyStoreECPublicKey(String str, int i, byte[] bArr, ECParameterSpec eCParameterSpec, ECPoint eCPoint) {
        super(str, i, KeyStoreConstants.SEC_KEY_ALGORITHM_EC, bArr);
        this.paramSpec = eCParameterSpec;
        this.point = eCPoint;
    }

    public HarmonyKeyStoreECPublicKey(String str, int i, ECPublicKey eCPublicKey) {
        this(str, i, eCPublicKey.getEncoded(), eCPublicKey.getParams(), eCPublicKey.getW());
        if (!"X.509".equalsIgnoreCase(eCPublicKey.getFormat())) {
            throw new IllegalArgumentException("Unsupported key export format: " + eCPublicKey.getFormat());
        }
    }

    @Override // java.security.interfaces.ECKey
    public ECParameterSpec getParams() {
        return this.paramSpec;
    }

    @Override // java.security.interfaces.ECPublicKey
    public ECPoint getW() {
        return this.point;
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStorePublicKey
    public PublicKey toAndroidPublicKey() {
        return new AndroidKeyStoreECPublicKey(getAlias(), getUid(), this);
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStorePublicKey, ohos.security.keystore.provider.HarmonyKeyStoreKey, java.lang.Object
    public int hashCode() {
        int hashCode = super.hashCode() * 31;
        ECParameterSpec eCParameterSpec = this.paramSpec;
        int i = 0;
        int hashCode2 = (hashCode + (eCParameterSpec == null ? 0 : eCParameterSpec.hashCode())) * 31;
        ECPoint eCPoint = this.point;
        if (eCPoint != null) {
            i = eCPoint.hashCode();
        }
        return hashCode2 + i;
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStorePublicKey, ohos.security.keystore.provider.HarmonyKeyStoreKey, java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        HarmonyKeyStoreECPublicKey harmonyKeyStoreECPublicKey = (HarmonyKeyStoreECPublicKey) obj;
        return this.paramSpec == harmonyKeyStoreECPublicKey.getParams() && this.point == harmonyKeyStoreECPublicKey.getW();
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
    }
}
