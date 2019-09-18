package com.huawei.nearbysdk.DTCP;

public interface RecvTransmitCallback {
    void onError(int i);

    void onProgress(int i);

    void onStatus(int i);

    void onSuccess(String[] strArr);
}
