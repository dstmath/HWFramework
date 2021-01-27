package com.huawei.nearbysdk.DTCP;

import com.huawei.nearbysdk.NearbyDevice;

public interface SendTransmitCallback {
    void onError(int i);

    void onHwIDHeadImageReceive(NearbyDevice nearbyDevice, byte[] bArr);

    void onImportProgress(int i);

    void onImportStarted();

    void onProgress(int i);

    void onSpeed(int i);

    void onStatus(int i);

    void onSuccess();

    void onTotalFileLength(long j);
}
