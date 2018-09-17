package com.android.org.bouncycastle.crypto.params;

public class DESParameters extends KeyParameter {
    public static final int DES_KEY_LENGTH = 8;
    private static byte[] DES_weak_keys = new byte[]{(byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 31, (byte) 31, (byte) 31, (byte) 31, (byte) 14, (byte) 14, (byte) 14, (byte) 14, (byte) -32, (byte) -32, (byte) -32, (byte) -32, (byte) -15, (byte) -15, (byte) -15, (byte) -15, (byte) -2, (byte) -2, (byte) -2, (byte) -2, (byte) -2, (byte) -2, (byte) -2, (byte) -2, (byte) 1, (byte) -2, (byte) 1, (byte) -2, (byte) 1, (byte) -2, (byte) 1, (byte) -2, (byte) 31, (byte) -32, (byte) 31, (byte) -32, (byte) 14, (byte) -15, (byte) 14, (byte) -15, (byte) 1, (byte) -32, (byte) 1, (byte) -32, (byte) 1, (byte) -15, (byte) 1, (byte) -15, (byte) 31, (byte) -2, (byte) 31, (byte) -2, (byte) 14, (byte) -2, (byte) 14, (byte) -2, (byte) 1, (byte) 31, (byte) 1, (byte) 31, (byte) 1, (byte) 14, (byte) 1, (byte) 14, (byte) -32, (byte) -2, (byte) -32, (byte) -2, (byte) -15, (byte) -2, (byte) -15, (byte) -2, (byte) -2, (byte) 1, (byte) -2, (byte) 1, (byte) -2, (byte) 1, (byte) -2, (byte) 1, (byte) -32, (byte) 31, (byte) -32, (byte) 31, (byte) -15, (byte) 14, (byte) -15, (byte) 14, (byte) -32, (byte) 1, (byte) -32, (byte) 1, (byte) -15, (byte) 1, (byte) -15, (byte) 1, (byte) -2, (byte) 31, (byte) -2, (byte) 31, (byte) -2, (byte) 14, (byte) -2, (byte) 14, (byte) 31, (byte) 1, (byte) 31, (byte) 1, (byte) 14, (byte) 1, (byte) 14, (byte) 1, (byte) -2, (byte) -32, (byte) -2, (byte) -32, (byte) -2, (byte) -15, (byte) -2, (byte) -15};
    private static final int N_DES_WEAK_KEYS = 16;

    public DESParameters(byte[] key) {
        super(key);
        if (isWeakKey(key, 0)) {
            throw new IllegalArgumentException("attempt to create weak DES key");
        }
    }

    public static boolean isWeakKey(byte[] key, int offset) {
        if (key.length - offset < 8) {
            throw new IllegalArgumentException("key material too short.");
        }
        int i = 0;
        while (i < 16) {
            int j = 0;
            while (j < 8) {
                if (key[j + offset] != DES_weak_keys[(i * 8) + j]) {
                    i++;
                } else {
                    j++;
                }
            }
            return true;
        }
        return false;
    }

    public static void setOddParity(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            bytes[i] = (byte) ((b & 254) | (((((((((b >> 1) ^ (b >> 2)) ^ (b >> 3)) ^ (b >> 4)) ^ (b >> 5)) ^ (b >> 6)) ^ (b >> 7)) ^ 1) & 1));
        }
    }
}
