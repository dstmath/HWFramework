package com.huawei.android.app;

import android.app.ActivityManager;
import android.os.RemoteException;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.app.IHwActivityManager.Stub;
import java.util.List;
import java.util.Map;

public class HwActivityManager {
    private static final Singleton<IHwActivityManager> IActivityManagerSingleton = new Singleton<IHwActivityManager>() {
        protected IHwActivityManager create() {
            try {
                return Stub.asInterface(ActivityManager.getService().getHwInnerService());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final String TAG = "HwActivityManager";

    public static IHwActivityManager getService() {
        return (IHwActivityManager) IActivityManagerSingleton.get();
    }

    public static void registerDAMonitorCallback(IHwDAMonitorCallback callback) {
        try {
            getService().registerDAMonitorCallback(callback);
        } catch (RemoteException e) {
            Log.e(TAG, "registerDAMonitorCallback failed: catch RemoteException!");
        }
    }

    public static void setCpusetSwitch(boolean enable) {
        try {
            getService().setCpusetSwitch(enable);
        } catch (RemoteException e) {
            Log.e(TAG, "setCpusetSwitch failed: catch RemoteException!");
        }
    }

    public static boolean cleanPackageRes(List<String> packageList, Map alarmTags, int targetUid, boolean cleanAlarm, boolean isNative, boolean hasPerceptAlarm) {
        try {
            return getService().cleanPackageRes(packageList, alarmTags, targetUid, cleanAlarm, isNative, hasPerceptAlarm);
        } catch (RemoteException e) {
            Log.e(TAG, "cleanPackageRes failed: catch RemoteException!");
            return false;
        }
    }
}
