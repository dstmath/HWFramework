package com.android.contacts.hap.numbermark.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;

public class NetUtils {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo infos = manager.getActiveNetworkInfo();
        if (infos == null || !infos.isAvailable()) {
            return false;
        }
        return State.CONNECTED == manager.getNetworkInfo(1).getState() || State.CONNECTED == manager.getNetworkInfo(0).getState();
    }

    public static String getTelimei(Context context) {
        return ((TelephonyManager) context.getSystemService("phone")).getDeviceId();
    }
}
