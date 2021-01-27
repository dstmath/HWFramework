package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.msg.CoMessengerClient;
import com.huawei.coauthservice.identitymgr.HwIdentityManager;
import com.huawei.coauthservice.identitymgr.callback.IConnectServiceCallback;

/* access modifiers changed from: package-private */
public class ConnectCoAuthServiceCallback implements CoMessengerClient.IConnectServiceCallback {
    CoAuth.IConnectServiceCallback callback;
    HwIdentityManager identityManager;

    ConnectCoAuthServiceCallback(CoAuth.IConnectServiceCallback callback2, HwIdentityManager identityManager2) {
        this.callback = callback2;
        this.identityManager = identityManager2;
    }

    @Override // com.huawei.coauth.msg.CoMessengerClient.IConnectServiceCallback
    public void onConnected() {
        Log.i(CoAuthUtil.TAG, "connectService connected");
        this.identityManager.connectService(new MyConnectServiceCallback(this.callback));
    }

    @Override // com.huawei.coauth.msg.CoMessengerClient.IConnectServiceCallback
    public void onConnectFailed() {
        Log.i(CoAuthUtil.TAG, "connectService connectFailed");
        this.callback.onConnectFailed();
    }

    @Override // com.huawei.coauth.msg.CoMessengerClient.IConnectServiceCallback
    public void onDisconnect() {
        Log.i(CoAuthUtil.TAG, "connectService disconnect");
        this.identityManager.disConnectService();
    }

    static class MyConnectServiceCallback implements IConnectServiceCallback {
        CoAuth.IConnectServiceCallback callback;

        MyConnectServiceCallback(CoAuth.IConnectServiceCallback callback2) {
            this.callback = callback2;
        }

        @Override // com.huawei.coauthservice.identitymgr.callback.IConnectServiceCallback
        public void onConnected() {
            Log.i(CoAuthUtil.TAG, "idm service is connected");
            this.callback.onConnected();
        }

        @Override // com.huawei.coauthservice.identitymgr.callback.IConnectServiceCallback
        public void onConnectFailed() {
            Log.e(CoAuthUtil.TAG, "idm service connection failed");
            this.callback.onConnectFailed();
        }

        @Override // com.huawei.coauthservice.identitymgr.callback.IConnectServiceCallback
        public void onDisconnect() {
            Log.i(CoAuthUtil.TAG, "idm service disconnected");
            this.callback.onDisconnect();
        }
    }
}
