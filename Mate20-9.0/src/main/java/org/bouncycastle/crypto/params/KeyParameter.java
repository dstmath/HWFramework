package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;

public class KeyParameter implements CipherParameters {
    private byte[] key;

    public KeyParameter(byte[] bArr) {
        this(bArr, 0, bArr.length);
    }

    public KeyParameter(byte[] bArr, int i, int i2) {
        this.key = new byte[i2];
        System.arraycopy(bArr, i, this.key, 0, i2);
    }

    public byte[] getKey() {
        return this.key;
    }
}
