package com.huawei.android.iaware;

import android.os.Bundle;
import android.os.RemoteException;
import android.rms.iaware.IDeviceSettingCallback;

public class IDeviceSettingCallbackEx {
    private IDeviceSettingCallback mDeviceSettingCallback = new IDeviceSettingCallback.Stub() {
        public void onDevSceneChanged(String packageName, int uid, int mode, Bundle data) throws RemoteException {
            IDeviceSettingCallbackEx.this.onDevSceneChanged(packageName, uid, mode, data);
        }
    };

    public void onDevSceneChanged(String packageName, int uid, int mode, Bundle data) {
    }

    public IDeviceSettingCallback getDeviceSettingCallback() {
        return this.mDeviceSettingCallback;
    }
}
