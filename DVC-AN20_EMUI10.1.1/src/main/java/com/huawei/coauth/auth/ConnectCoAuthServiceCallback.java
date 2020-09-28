package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.msg.CoMessengerClient;

/* access modifiers changed from: package-private */
public class ConnectCoAuthServiceCallback implements CoMessengerClient.IConnectServiceCallback {
    CoAuth.IConnectServiceCallback callback;

    ConnectCoAuthServiceCallback(CoAuth.IConnectServiceCallback callback2) {
        this.callback = callback2;
    }

    @Override // com.huawei.coauth.msg.CoMessengerClient.IConnectServiceCallback
    public void onConnected() {
        Log.i(CoAuthUtil.TAG, "connectService connected");
        this.callback.onConnected();
    }

    @Override // com.huawei.coauth.msg.CoMessengerClient.IConnectServiceCallback
    public void onConnectFailed() {
        Log.i(CoAuthUtil.TAG, "connectService connectFailed");
        this.callback.onConnectFailed();
    }

    @Override // com.huawei.coauth.msg.CoMessengerClient.IConnectServiceCallback
    public void onDisconnect() {
        Log.i(CoAuthUtil.TAG, "connectService disconnect");
        this.callback.onDisconnect();
    }
}
