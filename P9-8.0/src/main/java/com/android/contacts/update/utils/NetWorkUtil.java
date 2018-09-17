package com.android.contacts.update.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetWorkUtil {
    public static boolean checkConnectivityStatus(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info == null || !info.isAvailable()) {
            return false;
        }
        return true;
    }

    public static boolean isNetworkWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null && info.isAvailable() && info.getType() == 1) {
            return true;
        }
        return false;
    }

    public static boolean isNetworkMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null && info.isAvailable() && info.getType() == 0) {
            return true;
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm != null) {
            return cm.getActiveNetworkInfo();
        }
        return null;
    }
}
