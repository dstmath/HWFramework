package com.android.internal.telephony;

public interface IHwWspTypeDecoderEx {
    default boolean decodeForConnectwb(int startIndex, int mediaPrefixLength) {
        return false;
    }

    default byte[] getMacByte() {
        return null;
    }

    default int getSec() {
        return -1;
    }
}
