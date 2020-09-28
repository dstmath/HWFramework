package android.security.keystore;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;

public class AndroidKeyStoreRSAPublicKey extends AndroidKeyStorePublicKey implements RSAPublicKey {
    private final BigInteger mModulus;
    private final BigInteger mPublicExponent;

    public AndroidKeyStoreRSAPublicKey(String alias, int uid, byte[] x509EncodedForm, BigInteger modulus, BigInteger publicExponent) {
        super(alias, uid, KeyProperties.KEY_ALGORITHM_RSA, x509EncodedForm);
        this.mModulus = modulus;
        this.mPublicExponent = publicExponent;
    }

    public AndroidKeyStoreRSAPublicKey(String alias, int uid, RSAPublicKey info) {
        this(alias, uid, info.getEncoded(), info.getModulus(), info.getPublicExponent());
        if (!"X.509".equalsIgnoreCase(info.getFormat())) {
            throw new IllegalArgumentException("Unsupported key export format: " + info.getFormat());
        }
    }

    public BigInteger getModulus() {
        return this.mModulus;
    }

    public BigInteger getPublicExponent() {
        return this.mPublicExponent;
    }
}
