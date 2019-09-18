package javax.crypto.spec;

import java.security.InvalidKeyException;
import java.security.spec.KeySpec;

public class DESedeKeySpec implements KeySpec {
    public static final int DES_EDE_KEY_LEN = 24;
    private byte[] key;

    public DESedeKeySpec(byte[] key2) throws InvalidKeyException {
        this(key2, 0);
    }

    public DESedeKeySpec(byte[] key2, int offset) throws InvalidKeyException {
        if (key2.length - offset >= 24) {
            this.key = new byte[24];
            System.arraycopy(key2, offset, this.key, 0, 24);
            return;
        }
        throw new InvalidKeyException("Wrong key size");
    }

    public byte[] getKey() {
        return (byte[]) this.key.clone();
    }

    public static boolean isParityAdjusted(byte[] key2, int offset) throws InvalidKeyException {
        if (key2.length - offset < 24) {
            throw new InvalidKeyException("Wrong key size");
        } else if (!DESKeySpec.isParityAdjusted(key2, offset) || !DESKeySpec.isParityAdjusted(key2, offset + 8) || !DESKeySpec.isParityAdjusted(key2, offset + 16)) {
            return false;
        } else {
            return true;
        }
    }
}
