package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.CipherParameters;

public class KeyParameter implements CipherParameters {
    private byte[] key;

    public KeyParameter(byte[] key2) {
        this(key2, 0, key2.length);
    }

    public KeyParameter(byte[] key2, int keyOff, int keyLen) {
        this.key = new byte[keyLen];
        System.arraycopy(key2, keyOff, this.key, 0, keyLen);
    }

    public byte[] getKey() {
        return this.key;
    }
}
