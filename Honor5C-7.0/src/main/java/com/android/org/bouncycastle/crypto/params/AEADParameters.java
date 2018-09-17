package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.CipherParameters;

public class AEADParameters implements CipherParameters {
    private byte[] associatedText;
    private KeyParameter key;
    private int macSize;
    private byte[] nonce;

    public AEADParameters(KeyParameter key, int macSize, byte[] nonce) {
        this(key, macSize, nonce, null);
    }

    public AEADParameters(KeyParameter key, int macSize, byte[] nonce, byte[] associatedText) {
        this.key = key;
        this.nonce = nonce;
        this.macSize = macSize;
        this.associatedText = associatedText;
    }

    public KeyParameter getKey() {
        return this.key;
    }

    public int getMacSize() {
        return this.macSize;
    }

    public byte[] getAssociatedText() {
        return this.associatedText;
    }

    public byte[] getNonce() {
        return this.nonce;
    }
}
