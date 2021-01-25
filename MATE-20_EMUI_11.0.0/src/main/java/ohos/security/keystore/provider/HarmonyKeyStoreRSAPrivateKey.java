package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreRSAPrivateKey;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.interfaces.RSAKey;
import java.util.Objects;
import ohos.security.keystore.KeyStoreConstants;

public class HarmonyKeyStoreRSAPrivateKey extends HarmonyKeyStorePrivateKey implements RSAKey {
    private static final long serialVersionUID = -4065420650145968478L;
    private final BigInteger modulus;

    public HarmonyKeyStoreRSAPrivateKey(String str, int i, BigInteger bigInteger) {
        super(str, i, KeyStoreConstants.SEC_KEY_ALGORITHM_RSA);
        this.modulus = bigInteger;
    }

    @Override // java.security.interfaces.RSAKey
    public BigInteger getModulus() {
        return this.modulus;
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStorePrivateKey
    public PrivateKey toAndroidPrivateKey() {
        return new AndroidKeyStoreRSAPrivateKey(getAlias(), getUid(), this.modulus);
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStoreKey, java.lang.Object
    public int hashCode() {
        int hashCode = super.hashCode() * 31;
        BigInteger bigInteger = this.modulus;
        return hashCode + (bigInteger == null ? 0 : bigInteger.hashCode());
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStoreKey, java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (super.equals(obj) && getClass() == obj.getClass()) {
            return Objects.equals(this.modulus, ((HarmonyKeyStoreRSAPrivateKey) obj).modulus);
        }
        return false;
    }
}
