package com.huawei.android.app;

import android.app.IAlarmManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.app.IHwAlarmManager;

public class HwAlarmManager {
    private static final Singleton<IHwAlarmManager> IHwAlarmManagerSingleton = new Singleton<IHwAlarmManager>() {
        /* class com.huawei.android.app.HwAlarmManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwAlarmManager create() {
            try {
                IAlarmManager ams = IAlarmManager.Stub.asInterface(ServiceManager.getService("alarm"));
                if (ams == null) {
                    return null;
                }
                return IHwAlarmManager.Stub.asInterface(ams.getHwInnerService());
            } catch (RemoteException e) {
                Log.e(HwAlarmManager.TAG, "IHwAlarmManager create() fail: " + e);
                return null;
            }
        }
    };
    private static final String TAG = "HwAlarmManager";

    public static IHwAlarmManager getService() {
        return IHwAlarmManagerSingleton.get();
    }

    public static int getWakeUpNum(int uid, String pkg) {
        try {
            return getService().getWakeUpNum(uid, pkg);
        } catch (RemoteException e) {
            Log.e(TAG, "getWakeUpNum catch RemoteException!");
            return 0;
        }
    }

    public static long checkHasHwRTCAlarm(String packageName) {
        try {
            return getService().checkHasHwRTCAlarm(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "checkHasHwRTCAlarm catch RemoteException!");
            return -1;
        }
    }

    public static void adjustHwRTCAlarm(boolean deskClockTime, boolean bootOnTime, int typeState) {
        try {
            getService().adjustHwRTCAlarm(deskClockTime, bootOnTime, typeState);
        } catch (RemoteException e) {
            Log.e(TAG, "adjustHwRTCAlarm catch RemoteException!");
        }
    }

    public static void setHwRTCAlarm() {
        try {
            getService().setHwRTCAlarm();
        } catch (RemoteException e) {
            Log.e(TAG, "setHwRTCAlarm catch RemoteException!");
        }
    }
}
