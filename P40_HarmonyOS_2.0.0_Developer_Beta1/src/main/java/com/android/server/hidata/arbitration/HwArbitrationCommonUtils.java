package com.android.server.hidata.arbitration;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.wifi.HwHiLog;
import android.view.Display;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.telephony.SubscriptionManagerEx;

public class HwArbitrationCommonUtils {
    public static final boolean IS_HIDATA2_ENABLED = SystemProperties.getBoolean("ro.config.hidata2_on", true);
    public static final boolean MAINLAND_REGION = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final String TAG = "HiData_Arbitration";

    public static void logD(String tag, boolean isFmtStrPrivate, String log, Object... args) {
        HwHiLog.d(tag, isFmtStrPrivate, log, args);
    }

    public static void logI(String tag, boolean isFmtStrPrivate, String log, Object... args) {
        HwHiLog.i(tag, isFmtStrPrivate, log, args);
    }

    public static void logE(String tag, boolean isFmtStrPrivate, String log, Object... args) {
        HwHiLog.e(tag, isFmtStrPrivate, log, args);
    }

    public static int getActiveConnectType(Context mContext) {
        if (mContext == null) {
            logE(TAG, false, "getActiveConnectType: mContext is null", new Object[0]);
            return 802;
        }
        NetworkInfo activeNetInfo = ((ConnectivityManager) mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == 1 && activeNetInfo.isConnected()) {
            logD(TAG, false, "TYPE_WIFI is active", new Object[0]);
            return 800;
        } else if (activeNetInfo == null || activeNetInfo.getType() != 0 || !activeNetInfo.isConnected()) {
            logD(TAG, false, "ACTIVE_TYPE is none", new Object[0]);
            return 802;
        } else {
            logD(TAG, false, "TYPE_MOBILE is active", new Object[0]);
            return 801;
        }
    }

    public static boolean isWifiEnabled(Context mContext) {
        return Settings.Global.getInt(mContext.getContentResolver(), "wifi_on", 0) != 0;
    }

    public static boolean isWifiConnected(Context mContext) {
        NetworkInfo wifiNetInfo = ((ConnectivityManager) mContext.getSystemService("connectivity")).getNetworkInfo(1);
        if (wifiNetInfo == null || wifiNetInfo.getState() != NetworkInfo.State.CONNECTED) {
            return false;
        }
        return true;
    }

    public static boolean isScreenOn(Context mContext) {
        Display[] displays = ((DisplayManager) mContext.getSystemService("display")).getDisplays();
        for (Display display : displays) {
            if (display.getState() == 2 || display.getState() == 0) {
                logD(TAG, false, "display STATE is ON", new Object[0]);
                return true;
            }
        }
        return false;
    }

    public static boolean isActiveSubId(int subId) {
        if (!isSlotIdValid(getSlotId(subId))) {
            return false;
        }
        return true;
    }

    public static int getSlotId(int subId) {
        if (!isSubIdValid(subId)) {
            return -1;
        }
        return SubscriptionManagerEx.getSlotIndex(subId);
    }

    public static boolean isSlotIdValid(int slotId) {
        return slotId >= 0 && slotId < 2;
    }

    public static boolean isSubIdValid(int subId) {
        if (subId >= 0) {
            return true;
        }
        return false;
    }
}
