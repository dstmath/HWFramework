package com.huawei.android.os;

import android.os.Bundle;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IRemoteCallbackEx {
    private IRemoteCallback mIRemoteCallback;

    public IRemoteCallbackEx(IRemoteCallback remoteCallback) {
        this.mIRemoteCallback = remoteCallback;
    }

    @HwSystemApi
    public void sendResult(Bundle data) throws RemoteException {
        IRemoteCallback iRemoteCallback = this.mIRemoteCallback;
        if (iRemoteCallback != null) {
            iRemoteCallback.sendResult(data);
        }
    }

    public IRemoteCallback getIRemoteCallback() {
        return this.mIRemoteCallback;
    }
}
