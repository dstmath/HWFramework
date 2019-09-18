package com.android.internal.telephony;

public abstract class AbstractWspTypeDecoder {
    /* access modifiers changed from: protected */
    public boolean decodeForConnectwb(int startIndex, int mediaPrefixLength) {
        return false;
    }

    public byte[] getMacByte() {
        return new byte[]{0};
    }

    public int getSec() {
        return 0;
    }
}
