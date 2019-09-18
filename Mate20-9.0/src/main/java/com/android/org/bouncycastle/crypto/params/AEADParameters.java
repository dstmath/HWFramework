package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.CipherParameters;

public class AEADParameters implements CipherParameters {
    private byte[] associatedText;
    private KeyParameter key;
    private int macSize;
    private byte[] nonce;

    public AEADParameters(KeyParameter key2, int macSize2, byte[] nonce2) {
        this(key2, macSize2, nonce2, null);
    }

    public AEADParameters(KeyParameter key2, int macSize2, byte[] nonce2, byte[] associatedText2) {
        this.key = key2;
        this.nonce = nonce2;
        this.macSize = macSize2;
        this.associatedText = associatedText2;
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
