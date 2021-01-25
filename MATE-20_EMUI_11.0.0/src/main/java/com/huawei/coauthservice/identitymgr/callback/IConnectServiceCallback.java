package com.huawei.coauthservice.identitymgr.callback;

public interface IConnectServiceCallback {
    void onConnectFailed();

    void onConnected();

    void onDisconnect();
}
