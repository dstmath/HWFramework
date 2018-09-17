package android.security.keystore;

import java.security.PublicKey;
import java.util.Arrays;

public class AndroidKeyStorePublicKey extends AndroidKeyStoreKey implements PublicKey {
    private final byte[] mEncoded;

    public AndroidKeyStorePublicKey(String alias, int uid, String algorithm, byte[] x509EncodedForm) {
        super(alias, uid, algorithm);
        this.mEncoded = ArrayUtils.cloneIfNotEmpty(x509EncodedForm);
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] getEncoded() {
        return ArrayUtils.cloneIfNotEmpty(this.mEncoded);
    }

    public int hashCode() {
        return (super.hashCode() * 31) + Arrays.hashCode(this.mEncoded);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        return Arrays.equals(this.mEncoded, ((AndroidKeyStorePublicKey) obj).mEncoded);
    }
}
