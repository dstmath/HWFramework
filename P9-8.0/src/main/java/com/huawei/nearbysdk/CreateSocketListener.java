package com.huawei.nearbysdk;

public interface CreateSocketListener {
    void onCreateFail(int i);

    void onCreateSuccess(NearbySocket nearbySocket);
}
