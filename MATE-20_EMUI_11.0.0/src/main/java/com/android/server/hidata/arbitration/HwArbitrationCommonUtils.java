package com.android.server.hidata.arbitration;

import android.app.ActivityManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.wifi.HwHiLog;
import android.view.Display;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.telephony.SubscriptionManagerEx;
import java.util.List;

public class HwArbitrationCommonUtils {
    public static final boolean DEL_DEFAULT_LINK = SystemProperties.getBoolean("ro.config.del_default_link", true);
    public static final boolean IS_HIDATA2_ENABLED = SystemProperties.getBoolean("ro.config.hidata2_on", true);
    public static final boolean MAINLAND_REGION = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    public static final String TAG = "HiData_HwArbitrationCommonUtils";

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

    public static boolean isCellConnected(Context mContext) {
        NetworkInfo cellNetInfo = ((ConnectivityManager) mContext.getSystemService("connectivity")).getNetworkInfo(0);
        if (cellNetInfo == null || cellNetInfo.getState() != NetworkInfo.State.CONNECTED) {
            return false;
        }
        return true;
    }

    public static boolean isWifiConnected(Context mContext) {
        NetworkInfo wifiNetInfo = ((ConnectivityManager) mContext.getSystemService("connectivity")).getNetworkInfo(1);
        if (wifiNetInfo == null || wifiNetInfo.getState() != NetworkInfo.State.CONNECTED) {
            return false;
        }
        return true;
    }

    public static boolean isCellEnable(Context mContext) {
        return Settings.Global.getInt(mContext.getContentResolver(), "mobile_data", 0) != 0;
    }

    public static boolean isDataRoamingEnable(Context mContext) {
        boolean result = Settings.Global.getInt(mContext.getContentResolver(), "data_roaming", 0) != 0;
        logD(TAG, false, "DataRoamingStateEnable:%{public}s", String.valueOf(result));
        return result;
    }

    public static boolean hasSimCard(Context mContext) {
        if (((TelephonyManager) mContext.getSystemService("phone")).getSimState() != 5) {
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

    public static boolean isVicePhoneCalling(Context context) {
        boolean isCalling = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager == null) {
            logE(TAG, false, "isVicePhoneCalling: telephonyManager is null, return!", new Object[0]);
            return false;
        }
        if (telephonyManager.getPhoneCount() == 2) {
            int defaultDataSlotId = telephonyManager.getSlotIndex();
            if (!isSlotIdValid(defaultDataSlotId)) {
                return false;
            }
            int viceSlotId = defaultDataSlotId == 0 ? 1 : 0;
            if (telephonyManager.getSimState(viceSlotId) != 5) {
                logD(TAG, false, "SIM state not ready", new Object[0]);
                return false;
            }
            int viceSubId = getSubId(viceSlotId);
            if (!isSubIdValid(viceSubId)) {
                return false;
            }
            if (telephonyManager.getCallState(viceSubId) == 2 || telephonyManager.getCallState(viceSubId) == 1) {
                isCalling = true;
            }
        }
        logD(TAG, false, "isViceSIMCalling:%{public}s", String.valueOf(isCalling));
        return isCalling;
    }

    public static boolean isDefaultPhoneCsCalling(Context context) {
        boolean isCsCalling = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager == null) {
            logE(TAG, false, "isDefaultPhoneCsCalling: telephonyManager is null, return!", new Object[0]);
            return false;
        } else if (TelephonyManager.getNetworkClass(telephonyManager.getNetworkType()) >= 3) {
            logD(TAG, false, "Network is 4G", new Object[0]);
            return false;
        } else {
            int defaultSubId = SubscriptionManager.getDefaultSubId();
            if (telephonyManager.getSimState(telephonyManager.getSlotIndex()) == 5 && (telephonyManager.getCallState(defaultSubId) == 2 || telephonyManager.getCallState(defaultSubId) == 1)) {
                isCsCalling = true;
            }
            logD(TAG, false, "isDefaultSIMCSCalling:%{public}s", String.valueOf(isCsCalling));
            return isCsCalling;
        }
    }

    public static boolean isSubIdValid(int subId) {
        if (subId >= 0) {
            return true;
        }
        return false;
    }

    public static int getSlotId(int subId) {
        if (!isSubIdValid(subId)) {
            return -1;
        }
        return SubscriptionManagerEx.getSlotIndex(subId);
    }

    public static int getSubId(int slotId) {
        int[] subIds;
        if (isSlotIdValid(slotId) && (subIds = SubscriptionManagerEx.getSubId(slotId)) != null && subIds.length > 0) {
            return subIds[0];
        }
        return -1;
    }

    public static boolean isActiveSubId(int subId) {
        if (!isSlotIdValid(getSlotId(subId))) {
            return false;
        }
        return true;
    }

    public static boolean isSlotIdValid(int slotId) {
        return slotId >= 0 && 2 > slotId;
    }

    public static boolean isDataConnected(Context context) {
        return 2 == ((TelephonyManager) context.getSystemService("phone")).getDataState();
    }

    public static String getTopActivityPackageName(Context context) {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
            if (tasks != null) {
                if (!tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    if (topActivity == null) {
                        return null;
                    }
                    logD(TAG, false, "Top_Activity, pgName:%{public}s", topActivity.getPackageName());
                    logD(TAG, false, "Top_Activity, className:%{public}s", topActivity.getClassName());
                    return topActivity.getPackageName();
                }
            }
            logD(TAG, false, "Top_Activity,Null", new Object[0]);
            return null;
        } catch (Exception e) {
            logD(TAG, false, "Exception happened while getting topActivity PackageName", new Object[0]);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x001d  */
    public static int getForegroundAppUid(Context context) {
        List<ActivityManager.RunningAppProcessInfo> lr;
        if (context == null || (lr = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) == null) {
            return -1;
        }
        for (ActivityManager.RunningAppProcessInfo ra : lr) {
            if (ra.importance == 200 || ra.importance == 100) {
                return ra.uid;
            }
            while (r3.hasNext()) {
            }
        }
        return -1;
    }

    public static boolean isDataRoamingEnabled(Context context, int subId) {
        if (subId < 0 || subId > 1) {
            logD(TAG, false, "unvalid SubId", new Object[0]);
            return false;
        }
        String ROAMING_SIM = "data_roaming";
        if (1 == subId) {
            ROAMING_SIM = ROAMING_SIM + "_sim2";
        }
        return Settings.Global.getInt(context.getContentResolver(), ROAMING_SIM, 0) != 0;
    }

    public static String getAppNameUid(Context context, int uid) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList;
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager == null || (appProcessList = activityManager.getRunningAppProcesses()) == null || appProcessList.size() == 0) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.uid == uid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    public static long getUidWiFiBytes(Context context, int uid) {
        NetworkStats summaryStats = null;
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        try {
            NetworkStats summaryStats2 = ((NetworkStatsManager) context.getSystemService("netstats")).querySummary(1, "", 0, System.currentTimeMillis());
            long temp = 0;
            do {
                summaryStats2.getNextBucket(bucket);
                if (uid == bucket.getUid()) {
                    temp += bucket.getRxBytes();
                }
            } while (summaryStats2.hasNextBucket());
            summaryStats2.close();
            return temp;
        } catch (RemoteException | SecurityException e) {
            Log.e(TAG, e.getMessage());
            if (0 != 0) {
                summaryStats.close();
            }
            return 0;
        } catch (Throwable th) {
            if (0 != 0) {
                summaryStats.close();
            }
            throw th;
        }
    }
}
