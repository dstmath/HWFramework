package com.huawei.nearbysdk;

public interface ICreateSocketCallback {
    public static final int STATE_CLOSURE = -1;
    public static final int STATE_FAILURE = 1;
    public static final int STATE_SUCCESS = 0;

    void onStatusChange(int i, NearbySocket nearbySocket, int i2);
}
