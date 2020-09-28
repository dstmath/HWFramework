package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuthCount;
import com.huawei.coauth.msg.CoAuthCountMessageClient;

public class ConnectCoAuthCountServiceCallback implements CoAuthCountMessageClient.IConnectCoAuthCountServiceCallback {
    private static final String TAG = "ConnectCoAuthCountServiceCallback";
    CoAuthCount.IConnectServiceCallback callback;

    ConnectCoAuthCountServiceCallback(CoAuthCount.IConnectServiceCallback callback2) {
        this.callback = callback2;
    }

    @Override // com.huawei.coauth.msg.CoAuthCountMessageClient.IConnectCoAuthCountServiceCallback
    public void onConnectFailed() {
        Log.i(TAG, "ConnectCoAuthCountService onConnectFailed");
        this.callback.onConnectFailed();
    }

    @Override // com.huawei.coauth.msg.CoAuthCountMessageClient.IConnectCoAuthCountServiceCallback
    public void onConnected() {
        Log.i(TAG, "ConnectCoAuthCountService onConnected");
        this.callback.onConnected();
    }

    @Override // com.huawei.coauth.msg.CoAuthCountMessageClient.IConnectCoAuthCountServiceCallback
    public void onDisconnect() {
        Log.i(TAG, "ConnectCoAuthCountService onDisconnect");
        this.callback.onDisconnect();
    }
}
