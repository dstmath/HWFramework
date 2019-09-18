package com.android.server.security.panpay.factoryreset;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStatus {
    private static final String TAG = "NetworkStatus";

    public static boolean isActive(Context mContext) {
        try {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService("connectivity");
            if (cm != null) {
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info == null || !info.isConnected()) {
                    Log.e("NetworkStatus", "isNetWorkConnected: Not OK");
                } else {
                    Log.e("NetworkStatus", "isNetWorkConnected: OK");
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("NetworkStatus", "isNetWorkConnected Exception: " + e.getMessage());
        }
        Log.e("NetworkStatus", "isNetWorkConnected: FALSE");
        return false;
    }
}
