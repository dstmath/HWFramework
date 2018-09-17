package android.security.keystore;

import java.math.BigInteger;
import java.security.interfaces.RSAKey;

public class AndroidKeyStoreRSAPrivateKey extends AndroidKeyStorePrivateKey implements RSAKey {
    private final BigInteger mModulus;

    public AndroidKeyStoreRSAPrivateKey(String alias, int uid, BigInteger modulus) {
        super(alias, uid, KeyProperties.KEY_ALGORITHM_RSA);
        this.mModulus = modulus;
    }

    public BigInteger getModulus() {
        return this.mModulus;
    }
}
