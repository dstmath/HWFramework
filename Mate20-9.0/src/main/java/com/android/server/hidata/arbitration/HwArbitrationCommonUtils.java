package com.android.server.hidata.arbitration;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.List;

public class HwArbitrationCommonUtils {
    public static final boolean DEL_DEFAULT_LINK = SystemProperties.getBoolean("ro.config.del_default_link", true);
    public static final boolean MAINLAND_REGION = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    public static final String TAG = "HiData_HwArbitrationCommonUtils";

    public static void logD(String tag, String log) {
        Log.d(tag, log);
    }

    public static void logI(String tag, String log) {
        Log.i(tag, log);
    }

    public static void logE(String tag, String log) {
        Log.e(tag, log);
    }

    public static int getActiveConnectType(Context mContext) {
        if (mContext == null) {
            logE(TAG, "getActiveConnectType: mContext is null");
            return 802;
        }
        NetworkInfo activeNetInfo = ((ConnectivityManager) mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == 1 && activeNetInfo.isConnected()) {
            logD(TAG, "TYPE_WIFI is active");
            return 800;
        } else if (activeNetInfo == null || activeNetInfo.getType() != 0 || !activeNetInfo.isConnected()) {
            logD(TAG, "ACTIVE_TYPE is none");
            return 802;
        } else {
            logD(TAG, "TYPE_MOBILE is active");
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
        boolean z = false;
        if (Settings.Global.getInt(mContext.getContentResolver(), "data_roaming", 0) != 0) {
            z = true;
        }
        boolean result = z;
        logD(TAG, "DataRoamingStateEnable:" + result);
        return result;
    }

    public static boolean hasSimCard(Context mContext) {
        if (((TelephonyManager) mContext.getSystemService("phone")).getSimState() != 5) {
            return false;
        }
        return true;
    }

    public static boolean isScreenOn(Context mContext) {
        for (Display display : ((DisplayManager) mContext.getSystemService("display")).getDisplays()) {
            if (display.getState() == 2 || display.getState() == 0) {
                logD(TAG, "display STATE is ON");
                return true;
            }
        }
        return false;
    }

    public static boolean isVicePhoneCalling(Context context) {
        boolean isCalling = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        int viceSubId = 0;
        if (telephonyManager == null) {
            logE(TAG, "isVicePhoneCalling: telephonyManager is null, return!");
            return false;
        }
        if (2 == telephonyManager.getPhoneCount()) {
            if (SubscriptionManager.getDefaultSubId() == 0) {
                viceSubId = 1;
            }
            if (5 == telephonyManager.getSimState(viceSubId) && (2 == telephonyManager.getCallState(viceSubId) || 1 == telephonyManager.getCallState(viceSubId))) {
                isCalling = true;
            }
        }
        logD(TAG, "isViceSIMCalling:" + isCalling);
        return isCalling;
    }

    public static boolean isDefaultPhoneCSCalling(Context context) {
        boolean isCSCalling = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager == null) {
            logE(TAG, "isDefaultPhoneCSCalling: telephonyManager is null, return!");
            return false;
        } else if (telephonyManager.isVolteAvailable()) {
            logD(TAG, "isVolteAvailable:" + telephonyManager.isVolteAvailable());
            return false;
        } else {
            int mDefaultSubId = SubscriptionManager.getDefaultSubId();
            if (5 == telephonyManager.getSimState(mDefaultSubId) && (2 == telephonyManager.getCallState(mDefaultSubId) || 1 == telephonyManager.getCallState(mDefaultSubId))) {
                isCSCalling = true;
            }
            logD(TAG, "isDefaultSIMCSCalling:" + isCSCalling);
            return isCSCalling;
        }
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
                    logD(TAG, "Top_Activity, pgName:" + topActivity.getPackageName());
                    logD(TAG, "Top_Activity, className:" + topActivity.getClassName());
                    return topActivity.getPackageName();
                }
            }
            logD(TAG, "Top_Activity,Null");
            return null;
        } catch (Exception e) {
            logD(TAG, "Debug_Top_Activity:Failure to get topActivity PackageName " + e);
        }
    }

    public static int getForegroundAppUid(Context context) {
        if (context == null) {
            return -1;
        }
        List<ActivityManager.RunningAppProcessInfo> lr = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (lr == null) {
            return -1;
        }
        for (ActivityManager.RunningAppProcessInfo ra : lr) {
            if (ra.importance != 200) {
                if (ra.importance == 100) {
                }
            }
            return ra.uid;
        }
        return -1;
    }

    public static boolean isDataRoamingEnabled(Context context, int subId) {
        boolean z = false;
        if (subId < 0 || subId > 1) {
            logD(TAG, "unvalid SubId");
            return false;
        }
        String ROAMING_SIM = "data_roaming";
        if (1 == subId) {
            ROAMING_SIM = ROAMING_SIM + "_sim2";
        }
        if (Settings.Global.getInt(context.getContentResolver(), ROAMING_SIM, 0) != 0) {
            z = true;
        }
        return z;
    }

    public static String getAppNameUid(Context context, int uid) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager == null) {
            return "";
        }
        List<ActivityManager.RunningAppProcessInfo> appProcessList = activityManager.getRunningAppProcesses();
        if (appProcessList == null || appProcessList.size() == 0) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.uid == uid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    public static boolean isLiteProduct(Context context) {
        return context != null && "lite".equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "hw_wifipro_enable"));
    }
}
