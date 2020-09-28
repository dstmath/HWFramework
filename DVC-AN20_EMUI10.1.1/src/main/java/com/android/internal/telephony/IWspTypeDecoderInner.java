package com.android.internal.telephony;

public interface IWspTypeDecoderInner {
    boolean decodeIntegerValue(int i);

    int getDecodedDataLength();

    long getValue32();

    byte[] getWspData();

    void setDecodedDataLength(int i);

    void setStringValue(String str);
}
