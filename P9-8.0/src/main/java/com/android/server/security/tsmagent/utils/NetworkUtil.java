package com.android.server.security.tsmagent.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;

public class NetworkUtil {
    private static final int ANDROID_M_CODE = 23;

    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        }
        return true;
    }

    private static NetworkInfo getActiveNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        if (VERSION.SDK_INT < 23 || context.checkSelfPermission("android.permission.ACCESS_NETWORK_STATE") == 0) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
            if (cm != null) {
                return cm.getActiveNetworkInfo();
            }
        }
        return null;
    }
}
