package com.huawei.nearbysdk;

public interface AuthListener {
    void onAuthentificationResult(long j, boolean z, byte[] bArr, NearbyDevice nearbyDevice);
}
