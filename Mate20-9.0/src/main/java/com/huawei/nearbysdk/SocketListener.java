package com.huawei.nearbysdk;

public interface SocketListener {
    void onConnectRequest(ChannelCreateRequest channelCreateRequest);

    void onStatusChange(int i);
}
