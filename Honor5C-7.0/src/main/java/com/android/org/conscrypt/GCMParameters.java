package com.android.org.conscrypt;

public class GCMParameters {
    public final byte[] iv;
    public final int tLen;

    public GCMParameters(int tLen, byte[] iv) {
        this.tLen = tLen;
        this.iv = iv;
    }

    public int getTLen() {
        return this.tLen;
    }

    public byte[] getIV() {
        return this.iv;
    }
}
