package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;

public class RC5Parameters implements CipherParameters {
    private byte[] key;
    private int rounds;

    public RC5Parameters(byte[] bArr, int i) {
        if (bArr.length <= 255) {
            this.key = new byte[bArr.length];
            this.rounds = i;
            System.arraycopy(bArr, 0, this.key, 0, bArr.length);
            return;
        }
        throw new IllegalArgumentException("RC5 key length can be no greater than 255");
    }

    public byte[] getKey() {
        return this.key;
    }

    public int getRounds() {
        return this.rounds;
    }
}
