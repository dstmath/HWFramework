package com.huawei.nearbysdk;

import android.support.annotation.Nullable;

public interface ICreateSocketCallback {
    public static final int STATE_CLOSURE = -1;
    public static final int STATE_FAILURE = 1;
    public static final int STATE_SUCCESS = 0;

    void onStatusChange(int i, @Nullable NearbySocket nearbySocket, int i2);
}
