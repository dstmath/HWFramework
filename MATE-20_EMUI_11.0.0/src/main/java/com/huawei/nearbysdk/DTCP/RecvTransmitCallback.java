package com.huawei.nearbysdk.DTCP;

public interface RecvTransmitCallback {
    void onError(int i);

    void onImportProgress(int i);

    void onImportStarted();

    void onProgress(int i);

    void onSpeed(int i);

    void onStatus(int i);

    void onSuccess(String[] strArr);

    void onTotalFileLength(long j);
}
