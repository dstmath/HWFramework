package com.android.internal.telephony;

public abstract class AbstractWspTypeDecoder {
    protected boolean decodeForConnectwb(int startIndex, int mediaPrefixLength) {
        return false;
    }

    public byte[] getMacByte() {
        return new byte[]{(byte) 0};
    }

    public int getSec() {
        return 0;
    }
}
