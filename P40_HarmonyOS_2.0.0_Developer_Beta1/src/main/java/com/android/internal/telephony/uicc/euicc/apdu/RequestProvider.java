package com.android.internal.telephony.uicc.euicc.apdu;

public interface RequestProvider {
    void buildRequest(byte[] bArr, RequestBuilder requestBuilder) throws Throwable;
}
