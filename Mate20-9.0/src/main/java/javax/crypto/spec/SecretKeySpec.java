package javax.crypto.spec;

import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Locale;
import javax.crypto.SecretKey;

public class SecretKeySpec implements KeySpec, SecretKey {
    private static final long serialVersionUID = 6577238317307289933L;
    private String algorithm;
    private byte[] key;

    public SecretKeySpec(byte[] key2, String algorithm2) {
        if (key2 == null || algorithm2 == null) {
            throw new IllegalArgumentException("Missing argument");
        } else if (key2.length != 0) {
            this.key = (byte[]) key2.clone();
            this.algorithm = algorithm2;
        } else {
            throw new IllegalArgumentException("Empty key");
        }
    }

    public SecretKeySpec(byte[] key2, int offset, int len, String algorithm2) {
        if (key2 == null || algorithm2 == null) {
            throw new IllegalArgumentException("Missing argument");
        } else if (key2.length == 0) {
            throw new IllegalArgumentException("Empty key");
        } else if (key2.length - offset < len) {
            throw new IllegalArgumentException("Invalid offset/length combination");
        } else if (len >= 0) {
            this.key = new byte[len];
            System.arraycopy(key2, offset, this.key, 0, len);
            this.algorithm = algorithm2;
        } else {
            throw new ArrayIndexOutOfBoundsException("len is negative");
        }
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getFormat() {
        return "RAW";
    }

    public byte[] getEncoded() {
        return (byte[]) this.key.clone();
    }

    public int hashCode() {
        int retval = 0;
        for (int i = 1; i < this.key.length; i++) {
            retval += this.key[i] * i;
        }
        if (this.algorithm.equalsIgnoreCase("TripleDES")) {
            int hashCode = "desede".hashCode() ^ retval;
            int retval2 = hashCode;
            return hashCode;
        }
        int hashCode2 = this.algorithm.toLowerCase(Locale.ENGLISH).hashCode() ^ retval;
        int retval3 = hashCode2;
        return hashCode2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SecretKey)) {
            return false;
        }
        String thatAlg = ((SecretKey) obj).getAlgorithm();
        if (!thatAlg.equalsIgnoreCase(this.algorithm) && ((!thatAlg.equalsIgnoreCase("DESede") || !this.algorithm.equalsIgnoreCase("TripleDES")) && (!thatAlg.equalsIgnoreCase("TripleDES") || !this.algorithm.equalsIgnoreCase("DESede")))) {
            return false;
        }
        return MessageDigest.isEqual(this.key, ((SecretKey) obj).getEncoded());
    }
}
