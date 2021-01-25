package com.android.server.rms.iaware.dev;

import android.os.Bundle;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IDeviceSettingCallback;
import com.android.server.rms.iaware.feature.DevSchedFeatureRt;
import java.util.ArrayList;
import java.util.List;

public class DevSchedCallbackManager {
    private static final Object LOCK = new Object();
    private static final String TAG = "DevSchedCallbackManager";
    private static DevSchedCallbackManager sInstance;
    private final List<DevRemoteCallbackList> mCallbackList = new ArrayList();

    private DevSchedCallbackManager() {
    }

    public static DevSchedCallbackManager getInstance() {
        DevSchedCallbackManager devSchedCallbackManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new DevSchedCallbackManager();
            }
            devSchedCallbackManager = sInstance;
        }
        return devSchedCallbackManager;
    }

    public void registerDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        if (callback == null) {
            AwareLog.e(TAG, "register FAILED, callback is null error. input deviceId:" + deviceId);
        } else if (!DevSchedFeatureRt.isDeviceIdAvailable(deviceId)) {
            AwareLog.i(TAG, "register FAILED, deviceId is not available. input deviceId:" + deviceId);
        } else {
            synchronized (this.mCallbackList) {
                DevRemoteCallbackList devCallback = getDevRemoteCallback(deviceId);
                if (devCallback == null) {
                    devCallback = new DevRemoteCallbackList(deviceId);
                    this.mCallbackList.add(devCallback);
                }
                devCallback.registerDevModeMethod(callback, args);
            }
            AwareLog.d(TAG, "register SUCCESS, mCallbackList:" + toString());
            DevSchedFeatureRt.sendCurrentDeviceMode(deviceId);
        }
    }

    public void unregisterDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        if (callback == null) {
            AwareLog.e(TAG, "unregister FAILED, callback is null error. input deviceId:" + deviceId);
            return;
        }
        synchronized (this.mCallbackList) {
            DevRemoteCallbackList devCallback = getDevRemoteCallback(deviceId);
            if (devCallback == null) {
                AwareLog.i(TAG, "unregister FAILED, Non-Registered device, input deviceId:" + deviceId);
                return;
            }
            devCallback.unregisterDevModeMethod(callback, args);
            if (devCallback.getCount() == 0) {
                this.mCallbackList.remove(devCallback);
            }
            AwareLog.d(TAG, "unregister SUCCESS, mCallbackList:" + toString());
        }
    }

    public void sendDeviceMode(int deviceId, String packageName, int uid, int mode, Bundle bundle) {
        DevRemoteCallbackList devCallback;
        synchronized (this.mCallbackList) {
            devCallback = getDevRemoteCallback(deviceId);
        }
        if (devCallback == null) {
            AwareLog.d(TAG, "send device mode FAILED, Non-Registered device, input deviceId:" + deviceId);
            return;
        }
        devCallback.sendDeviceMode(packageName, uid, mode, bundle);
        AwareLog.i(TAG, "send device mode SUCCESS. deviceId:" + deviceId + ", packageName:" + packageName + ", mode:" + mode);
    }

    private DevRemoteCallbackList getDevRemoteCallback(int deviceId) {
        for (DevRemoteCallbackList callback : this.mCallbackList) {
            if (callback != null && deviceId == callback.getDeviceId()) {
                return callback;
            }
        }
        return null;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        synchronized (this.mCallbackList) {
            sb.append("registered num :");
            sb.append(this.mCallbackList.size());
            sb.append(" [ ");
            sb.append(this.mCallbackList);
            sb.append(" ] ");
        }
        return sb.toString();
    }
}
