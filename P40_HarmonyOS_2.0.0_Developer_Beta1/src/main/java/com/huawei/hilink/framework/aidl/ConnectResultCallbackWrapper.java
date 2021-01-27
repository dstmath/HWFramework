package com.huawei.hilink.framework.aidl;

import android.os.RemoteException;
import com.huawei.hilink.framework.aidl.IConnectResultCallback;

public class ConnectResultCallbackWrapper extends IConnectResultCallback.Stub {
    @Override // com.huawei.hilink.framework.aidl.IConnectResultCallback
    public void onConnectSuccess(int requestId) throws RemoteException {
    }

    @Override // com.huawei.hilink.framework.aidl.IConnectResultCallback
    public void onConnectFailed(int requestId, int errorCode) throws RemoteException {
    }
}
