package com.huawei.hilink.framework.aidl;

import android.os.RemoteException;
import com.huawei.hilink.framework.aidl.IConnectionStateCallback;

public class ConnectionStateCallbackWrapper extends IConnectionStateCallback.Stub {
    private final HilinkServiceProxyState proxyState;

    public ConnectionStateCallbackWrapper(HilinkServiceProxyState proxyState2) {
        this.proxyState = proxyState2;
    }

    @Override // com.huawei.hilink.framework.aidl.IConnectionStateCallback
    public void onProxyState(int state) throws RemoteException {
        HilinkServiceProxyState hilinkServiceProxyState = this.proxyState;
        if (hilinkServiceProxyState != null) {
            hilinkServiceProxyState.onProxyReady(state);
        }
    }

    @Override // com.huawei.hilink.framework.aidl.IConnectionStateCallback
    public void onConnectionState(int state) throws RemoteException {
        HilinkServiceProxyState hilinkServiceProxyState = this.proxyState;
        if (hilinkServiceProxyState != null) {
            hilinkServiceProxyState.onConnectionState(state);
        }
    }
}
