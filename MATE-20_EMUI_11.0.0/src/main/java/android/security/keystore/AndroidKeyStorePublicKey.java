package android.security.keystore;

import java.security.PublicKey;
import java.util.Arrays;

public class AndroidKeyStorePublicKey extends AndroidKeyStoreKey implements PublicKey {
    private final byte[] mEncoded;

    public AndroidKeyStorePublicKey(String alias, int uid, String algorithm, byte[] x509EncodedForm) {
        super(alias, uid, algorithm);
        this.mEncoded = ArrayUtils.cloneIfNotEmpty(x509EncodedForm);
    }

    @Override // android.security.keystore.AndroidKeyStoreKey, java.security.Key
    public String getFormat() {
        return "X.509";
    }

    @Override // android.security.keystore.AndroidKeyStoreKey, java.security.Key
    public byte[] getEncoded() {
        return ArrayUtils.cloneIfNotEmpty(this.mEncoded);
    }

    @Override // android.security.keystore.AndroidKeyStoreKey, java.lang.Object
    public int hashCode() {
        return (super.hashCode() * 31) + Arrays.hashCode(this.mEncoded);
    }

    @Override // android.security.keystore.AndroidKeyStoreKey, java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (super.equals(obj) && getClass() == obj.getClass() && Arrays.equals(this.mEncoded, ((AndroidKeyStorePublicKey) obj).mEncoded)) {
            return true;
        }
        return false;
    }
}
