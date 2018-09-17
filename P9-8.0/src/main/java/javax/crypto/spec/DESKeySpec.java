package javax.crypto.spec;

import java.security.InvalidKeyException;
import java.security.spec.KeySpec;

public class DESKeySpec implements KeySpec {
    public static final int DES_KEY_LEN = 8;
    private static final byte[][] WEAK_KEYS = new byte[][]{new byte[]{(byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1}, new byte[]{(byte) -2, (byte) -2, (byte) -2, (byte) -2, (byte) -2, (byte) -2, (byte) -2, (byte) -2}, new byte[]{(byte) 31, (byte) 31, (byte) 31, (byte) 31, (byte) 14, (byte) 14, (byte) 14, (byte) 14}, new byte[]{(byte) -32, (byte) -32, (byte) -32, (byte) -32, (byte) -15, (byte) -15, (byte) -15, (byte) -15}, new byte[]{(byte) 1, (byte) -2, (byte) 1, (byte) -2, (byte) 1, (byte) -2, (byte) 1, (byte) -2}, new byte[]{(byte) 31, (byte) -32, (byte) 31, (byte) -32, (byte) 14, (byte) -15, (byte) 14, (byte) -15}, new byte[]{(byte) 1, (byte) -32, (byte) 1, (byte) -32, (byte) 1, (byte) -15, (byte) 1, (byte) -15}, new byte[]{(byte) 31, (byte) -2, (byte) 31, (byte) -2, (byte) 14, (byte) -2, (byte) 14, (byte) -2}, new byte[]{(byte) 1, (byte) 31, (byte) 1, (byte) 31, (byte) 1, (byte) 14, (byte) 1, (byte) 14}, new byte[]{(byte) -32, (byte) -2, (byte) -32, (byte) -2, (byte) -15, (byte) -2, (byte) -15, (byte) -2}, new byte[]{(byte) -2, (byte) 1, (byte) -2, (byte) 1, (byte) -2, (byte) 1, (byte) -2, (byte) 1}, new byte[]{(byte) -32, (byte) 31, (byte) -32, (byte) 31, (byte) -15, (byte) 14, (byte) -15, (byte) 14}, new byte[]{(byte) -32, (byte) 1, (byte) -32, (byte) 1, (byte) -15, (byte) 1, (byte) -15, (byte) 1}, new byte[]{(byte) -2, (byte) 31, (byte) -2, (byte) 31, (byte) -2, (byte) 14, (byte) -2, (byte) 14}, new byte[]{(byte) 31, (byte) 1, (byte) 31, (byte) 1, (byte) 14, (byte) 1, (byte) 14, (byte) 1}, new byte[]{(byte) -2, (byte) -32, (byte) -2, (byte) -32, (byte) -2, (byte) -15, (byte) -2, (byte) -15}};
    private byte[] key;

    public DESKeySpec(byte[] key) throws InvalidKeyException {
        this(key, 0);
    }

    public DESKeySpec(byte[] key, int offset) throws InvalidKeyException {
        if (key.length - offset < 8) {
            throw new InvalidKeyException("Wrong key size");
        }
        this.key = new byte[8];
        System.arraycopy(key, offset, this.key, 0, 8);
    }

    public byte[] getKey() {
        return (byte[]) this.key.clone();
    }

    public static boolean isParityAdjusted(byte[] key, int offset) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("null key");
        } else if (key.length - offset < 8) {
            throw new InvalidKeyException("Wrong key size");
        } else {
            int i = 0;
            int offset2 = offset;
            while (i < 8) {
                offset = offset2 + 1;
                if ((Integer.bitCount(key[offset2] & 255) & 1) == 0) {
                    return false;
                }
                i++;
                offset2 = offset;
            }
            return true;
        }
    }

    public static boolean isWeak(byte[] key, int offset) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("null key");
        } else if (key.length - offset < 8) {
            throw new InvalidKeyException("Wrong key size");
        } else {
            for (byte[] bArr : WEAK_KEYS) {
                boolean found = true;
                for (int j = 0; j < 8 && found; j++) {
                    if (bArr[j] != key[j + offset]) {
                        found = false;
                    }
                }
                if (found) {
                    return found;
                }
            }
            return false;
        }
    }
}
