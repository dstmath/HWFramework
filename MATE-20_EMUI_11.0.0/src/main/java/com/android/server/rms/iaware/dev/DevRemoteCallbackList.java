package com.android.server.rms.iaware.dev;

import android.os.Bundle;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IDeviceSettingCallback;

public class DevRemoteCallbackList {
    private static final String TAG = "DevRemoteCallbackList";
    private final RemoteCallbackList<IDeviceSettingCallback> mDevCallbacks = new RemoteCallbackList<>();
    private int mDeviceId;

    public DevRemoteCallbackList(int deviceId) {
        this.mDeviceId = deviceId;
    }

    public void registerDevModeMethod(IDeviceSettingCallback callback, Bundle args) {
        synchronized (this.mDevCallbacks) {
            this.mDevCallbacks.register(callback);
        }
    }

    public void unregisterDevModeMethod(IDeviceSettingCallback callback, Bundle args) {
        synchronized (this.mDevCallbacks) {
            this.mDevCallbacks.unregister(callback);
        }
    }

    public void sendDeviceMode(String packageName, int uid, int mode, Bundle bundle) {
        synchronized (this.mDevCallbacks) {
            int id = this.mDevCallbacks.beginBroadcast();
            while (id > 0) {
                id--;
                IDeviceSettingCallback callback = this.mDevCallbacks.getBroadcastItem(id);
                if (callback != null) {
                    try {
                        callback.onDevSceneChanged(packageName, uid, mode, bundle);
                    } catch (RemoteException e) {
                        AwareLog.e(TAG, "onDevSceneChanged remoteException");
                    }
                }
            }
            this.mDevCallbacks.finishBroadcast();
        }
    }

    public int getDeviceId() {
        return this.mDeviceId;
    }

    public int getCount() {
        int registeredCallbackCount;
        synchronized (this.mDevCallbacks) {
            registeredCallbackCount = this.mDevCallbacks.getRegisteredCallbackCount();
        }
        return registeredCallbackCount;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("mDeviceId :");
        sb.append(this.mDeviceId);
        sb.append(", RemoteCallbackList num :");
        sb.append(getCount());
        return sb.toString();
    }
}
