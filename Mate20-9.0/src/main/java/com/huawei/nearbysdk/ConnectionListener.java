package com.huawei.nearbysdk;

public interface ConnectionListener {
    void onCreateFail(NearbyDevice nearbyDevice, int i, int i2);

    void onCreateSuccess(NearbySession nearbySession);

    void onReceiveData(NearbySession nearbySession, byte[] bArr);

    void onReceiveSession(NearbySession nearbySession);

    void onStatusChange(int i);
}
