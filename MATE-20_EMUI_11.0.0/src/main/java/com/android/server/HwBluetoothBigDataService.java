package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.List;

public class HwBluetoothBigDataService {
    public static final String BIGDATA_RECEIVER_PACKAGENAME = "com.android.bluetooth";
    public static final String BLUETOOTH_BIGDATA = "com.android.bluetooth.bluetoothBigdata";
    private static final String TAG = "HwBluetoothBigDataService";
    private static HwBluetoothBigDataService mHwBluetoothBigDataService = null;
    boolean isChinaArea = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));

    public static synchronized HwBluetoothBigDataService getHwBluetoothBigDataService() {
        HwBluetoothBigDataService hwBluetoothBigDataService;
        synchronized (HwBluetoothBigDataService.class) {
            if (mHwBluetoothBigDataService == null) {
                mHwBluetoothBigDataService = new HwBluetoothBigDataService();
            }
            hwBluetoothBigDataService = mHwBluetoothBigDataService;
        }
        return hwBluetoothBigDataService;
    }

    public void sendBigDataEvent(Context mContext, String bigDataEvent) {
        if (this.isChinaArea && getAppName(mContext, Binder.getCallingPid()) != null) {
            Intent intent = new Intent();
            intent.setAction(bigDataEvent);
            intent.setPackage(BIGDATA_RECEIVER_PACKAGENAME);
            intent.putExtra("appName", getAppName(mContext, Binder.getCallingPid()));
            mContext.sendBroadcastAsUser(intent, UserHandle.of(UserHandle.getCallingUserId()), BLUETOOTH_BIGDATA);
        }
    }

    public String getAppName(Context mContext, int pid) {
        ActivityManager activityManager;
        List<ActivityManager.RunningAppProcessInfo> appProcesses;
        if (pid <= 0 || (activityManager = (ActivityManager) mContext.getSystemService("activity")) == null || (appProcesses = activityManager.getRunningAppProcesses()) == null || appProcesses.size() == 0) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid && appProcess.importance == 100) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
