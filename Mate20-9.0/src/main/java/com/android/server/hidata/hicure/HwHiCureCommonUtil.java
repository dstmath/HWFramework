package com.android.server.hidata.hicure;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

public class HwHiCureCommonUtil {
    public static final int BLOCK_LEVEL_EXTREMELY_BAD = 1;
    public static final int BLOCK_LEVEL_SLIGHTLY_BAD = 0;
    public static final int INVALID_VALUE = -1;
    public static final String TAG = "HwHiCureCommonUtil";

    public static boolean isUserDataEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) != 0;
    }

    public static boolean isDataConnected(Context context) {
        if (2 == TelephonyManager.getDefault().getDataState()) {
            return true;
        }
        return false;
    }

    public static boolean isDataRoamingEnabled(Context context) {
        int defaultDataSubId = getDefaultDataSubId(context);
        boolean z = false;
        if (defaultDataSubId < 0 || defaultDataSubId > 1) {
            Log.e(TAG, "invalid SubId");
            return false;
        }
        String ROAMING_SIM = "data_roaming";
        if (1 == defaultDataSubId) {
            ROAMING_SIM = ROAMING_SIM + "_sim2";
        }
        if (Settings.Global.getInt(context.getContentResolver(), ROAMING_SIM, 0) != 0) {
            z = true;
        }
        return z;
    }

    public static boolean isRoaming(Context context) {
        return TelephonyManager.getDefault().isNetworkRoaming(getDefaultDataSubId(context));
    }

    public static boolean isWifiEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "wifi_on", 0) != 0;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        boolean z = false;
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        int networkType = networkInfo.getType();
        if (networkInfo.isConnected() && networkType == 1) {
            z = true;
        }
        return z;
    }

    public static int getDefaultDataSubId(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "multi_sim_data_call", 0);
    }

    public static int getActiveConnectType(Context context) {
        if (context == null) {
            Log.e(TAG, "getActiveConnectType: context is null");
            return -1;
        }
        NetworkInfo activeNetInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetInfo == null) {
            Log.w(TAG, "getActiveConnectType: activeNetInfo is null");
            return -1;
        }
        int type = activeNetInfo.getType();
        if (activeNetInfo.isConnected()) {
            return type;
        }
        return -1;
    }
}
