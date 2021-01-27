package com.huawei.airsharing.api;

public interface IKitServiceStatusListener {
    public static final int EVENT_ID_PLAYER_SERVICE_CONNECTED = 1000;

    void onKitEvent(int i);
}
