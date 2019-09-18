package javax.crypto.spec;

import java.security.InvalidKeyException;
import java.security.spec.KeySpec;

public class DESKeySpec implements KeySpec {
    public static final int DES_KEY_LEN = 8;
    private static final byte[][] WEAK_KEYS = {new byte[]{1, 1, 1, 1, 1, 1, 1, 1}, new byte[]{-2, -2, -2, -2, -2, -2, -2, -2}, new byte[]{31, 31, 31, 31, 14, 14, 14, 14}, new byte[]{-32, -32, -32, -32, -15, -15, -15, -15}, new byte[]{1, -2, 1, -2, 1, -2, 1, -2}, new byte[]{31, -32, 31, -32, 14, -15, 14, -15}, new byte[]{1, -32, 1, -32, 1, -15, 1, -15}, new byte[]{31, -2, 31, -2, 14, -2, 14, -2}, new byte[]{1, 31, 1, 31, 1, 14, 1, 14}, new byte[]{-32, -2, -32, -2, -15, -2, -15, -2}, new byte[]{-2, 1, -2, 1, -2, 1, -2, 1}, new byte[]{-32, 31, -32, 31, -15, 14, -15, 14}, new byte[]{-32, 1, -32, 1, -15, 1, -15, 1}, new byte[]{-2, 31, -2, 31, -2, 14, -2, 14}, new byte[]{31, 1, 31, 1, 14, 1, 14, 1}, new byte[]{-2, -32, -2, -32, -2, -15, -2, -15}};
    private byte[] key;

    public DESKeySpec(byte[] key2) throws InvalidKeyException {
        this(key2, 0);
    }

    public DESKeySpec(byte[] key2, int offset) throws InvalidKeyException {
        if (key2.length - offset >= 8) {
            this.key = new byte[8];
            System.arraycopy(key2, offset, this.key, 0, 8);
            return;
        }
        throw new InvalidKeyException("Wrong key size");
    }

    public byte[] getKey() {
        return (byte[]) this.key.clone();
    }

    public static boolean isParityAdjusted(byte[] key2, int offset) throws InvalidKeyException {
        if (key2 == null) {
            throw new InvalidKeyException("null key");
        } else if (key2.length - offset >= 8) {
            int offset2 = offset;
            int i = 0;
            while (i < 8) {
                int offset3 = offset2 + 1;
                if ((Integer.bitCount(key2[offset2] & 255) & 1) == 0) {
                    return false;
                }
                i++;
                offset2 = offset3;
            }
            return true;
        } else {
            throw new InvalidKeyException("Wrong key size");
        }
    }

    public static boolean isWeak(byte[] key2, int offset) throws InvalidKeyException {
        if (key2 == null) {
            throw new InvalidKeyException("null key");
        } else if (key2.length - offset >= 8) {
            for (int i = 0; i < WEAK_KEYS.length; i++) {
                boolean found = true;
                for (int j = 0; j < 8 && found; j++) {
                    if (WEAK_KEYS[i][j] != key2[j + offset]) {
                        found = false;
                    }
                }
                if (found) {
                    return found;
                }
            }
            return false;
        } else {
            throw new InvalidKeyException("Wrong key size");
        }
    }
}
