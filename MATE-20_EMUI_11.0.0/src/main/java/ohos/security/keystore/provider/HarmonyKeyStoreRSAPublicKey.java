package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreRSAPublicKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;
import ohos.security.keystore.KeyStoreConstants;

public class HarmonyKeyStoreRSAPublicKey extends HarmonyKeyStorePublicKey implements RSAPublicKey {
    private static final long serialVersionUID = 7196876654145465740L;
    private final BigInteger modulus;
    private final BigInteger publicExponent;

    public HarmonyKeyStoreRSAPublicKey(String str, int i, byte[] bArr, BigInteger bigInteger, BigInteger bigInteger2) {
        super(str, i, KeyStoreConstants.SEC_KEY_ALGORITHM_RSA, bArr);
        this.modulus = bigInteger;
        this.publicExponent = bigInteger2;
    }

    public HarmonyKeyStoreRSAPublicKey(String str, int i, RSAPublicKey rSAPublicKey) {
        this(str, i, rSAPublicKey.getEncoded(), rSAPublicKey.getModulus(), rSAPublicKey.getPublicExponent());
        if (!"X.509".equalsIgnoreCase(rSAPublicKey.getFormat())) {
            throw new IllegalArgumentException("Unsupported key export format: " + rSAPublicKey.getFormat());
        }
    }

    @Override // java.security.interfaces.RSAKey
    public BigInteger getModulus() {
        return this.modulus;
    }

    @Override // java.security.interfaces.RSAPublicKey
    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStorePublicKey
    public PublicKey toAndroidPublicKey() {
        return new AndroidKeyStoreRSAPublicKey(getAlias(), getUid(), this);
    }

    @Override // ohos.security.keystore.provider.HarmonyKeyStorePublicKey, ohos.security.keystore.provider.HarmonyKeyStoreKey, java.lang.Object
    public int hashCode() {
        int hashCode = super.hashCode() * 31;
        BigInteger bigInteger = this.modulus;
        int i = 0;
        int hashCode2 = (hashCode + (bigInteger == null ? 0 : bigInteger.hashCode())) * 31;
        BigInteger bigInteger2 = this.publicExponent;
        if (bigInteger2 != null) {
            i = bigInteger2.hashCode();
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
        HarmonyKeyStoreRSAPublicKey harmonyKeyStoreRSAPublicKey = (HarmonyKeyStoreRSAPublicKey) obj;
        if (!Objects.equals(this.modulus, harmonyKeyStoreRSAPublicKey.modulus)) {
            return false;
        }
        return Objects.equals(this.publicExponent, harmonyKeyStoreRSAPublicKey.publicExponent);
    }
}
