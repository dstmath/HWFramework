package com.huawei.android.os;

import android.os.Bundle;
import android.os.IRemoteCallback;
import android.os.RemoteException;

public class IRemoteCallbackExt {
    private IRemoteCallback mIRemoteCallback;

    public void setIRemoteCallback(IRemoteCallback iRemoteCallback) {
        this.mIRemoteCallback = iRemoteCallback;
    }

    public void sendResult(Bundle data) throws RemoteException {
        IRemoteCallback iRemoteCallback = this.mIRemoteCallback;
        if (iRemoteCallback != null) {
            iRemoteCallback.sendResult(data);
        }
    }
}
